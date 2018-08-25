package onesignal

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.google.inject.Inject
import common.serialization.{SnakeCaseJsonProtocol, _}
import common.{ConfigReader, Logging}
import model.internal.{Order, User, UserType}
import model.internal.UserType._
import onesignal.ActionState._
import model.internal.UserType.{APPLICANT, CARRIER, PARTICIPANT}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import onesignal.EventType.{EventType, _}
import onesignal.HTML._
import play.api.libs.ws.WSClient
import service.Exception.BadRequestCodes._
import service.Exception.ShippearException

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class OneSignalClient @Inject()(client: WSClient)(implicit ec: ExecutionContext) extends ConfigReader with Logging with SnakeCaseJsonProtocol {

  private val config = envConfiguration.getConfig("email-notification").as[OneSignalConfig]
  private var active = config.activated
  private val appId = config.id.getOrElse("")
  private val auth = config.auth.getOrElse("")

  private val ContentType = ("Content-Type", "application/json;charset=utf-8")
  private val Authorization = ("Authorization", s"Basic $auth")

  //Paths Rest API One Signal
  val BasePath = "https://onesignal.com/api/v1"
  val NotificationPath = s"$BasePath/notifications"
  val AddDevice = s"$BasePath/players"
  val viewDevices = s"$AddDevice?app_id=$appId"
  def viewDevice(id: String) = s"$AddDevice/$id?app_id=$appId"

  def activated(state: Boolean): Boolean = {
    active = state
    active
  }

  private def emailBody(emailType: EventType): String = {
    emailType match {
      case ORDER_CREATED => CREATED
      case ORDER_WITH_CARRIER => WITH_CARRIER
      case ORDER_ON_WAY => TRAVELLING
      case ORDER_CANCELED => CANCELED
      case ORDER_FINALIZED => FINALIZED
    }
  }

  def messageFactory(order: Order, eventType: EventType, userCancelledType: Option[UserType]): Map[UserType, (String, String, Boolean)] = {

    val applicantFullName = s"${order.applicant.firstName} ${order.applicant.lastName}"
    val applicantPhoto = order.applicant.photoUrl

    val participantFullName = s"${order.participant.firstName} ${order.participant.lastName}"
    val participantPhoto = order.participant.photoUrl

    val carrierFullName = order.carrier.map{carrier => s"${carrier.firstName} ${carrier.lastName}"}.getOrElse("")
    val carrierPhoto = order.carrier.map(_.photoUrl).getOrElse("")

    val orderDescription = order.description

    lazy val cancelledFullName = userCancelledType match {
      case Some(APPLICANT) => applicantFullName
      case Some(PARTICIPANT) => participantFullName
      case Some(CARRIER) => carrierFullName
      case _ => "Shippear"
    }

    eventType match {
      //Order created
      case ORDER_CREATED => Map(PARTICIPANT ->
        (s"$applicantFullName quiere que seas participante de: $orderDescription", applicantPhoto, false))

      //Confirmed Participant
      case CONFIRM_PARTICIPANT => Map(APPLICANT ->
        (s"$participantFullName ha aceptado tu solicitud", participantPhoto, false))

      //Confirmed Carrier
      case ORDER_WITH_CARRIER =>
        Map(PARTICIPANT -> (s"$carrierFullName sera el transportista de: $orderDescription", carrierPhoto, false),
            APPLICANT -> (s"$carrierFullName sera el transportista de la solicitud que participas con $participantFullName de: $orderDescription", carrierPhoto, false),
          CARRIER -> (s"Has sido asignado al pedido #${order.orderNumber} correctamente", carrierPhoto, false))

      //Carrier validated by QR
      case ORDER_ON_WAY =>
        val messageFromCarrier =  s"$carrierFullName ha retirado el objeto $orderDescription y se encuentra en camino!"
        Map(APPLICANT -> (messageFromCarrier, carrierPhoto, false),
          PARTICIPANT -> (messageFromCarrier, carrierPhoto, false))

      //Canceled by some user
      case ORDER_CANCELED =>
        userCancelledType
          .map{ userType => UserType.values.filterNot(u => u.toString.equals(userType.toString))}
          .getOrElse(UserType.values)
          .map { u =>
            val photo = userCancelledType match {
              case APPLICANT => applicantPhoto
              case PARTICIPANT => participantPhoto
              case CARRIER => carrierPhoto
              case _ => ShippearLogo.logo
            }
            u -> (s"El pedido de $orderDescription fue cancelado por $cancelledFullName", photo, false)}
          .toMap

      case ORDER_FINALIZED =>
        val messageFromCarrier = s"La solicitud de $orderDescription se ha completado con exito!"
        Map(APPLICANT -> (messageFromCarrier, carrierPhoto, false),
          PARTICIPANT -> (messageFromCarrier, carrierPhoto, false),
          CARRIER -> (s"La solicitud #${order.orderNumber} se ha completado con exito!", carrierPhoto, false))

      case AUX_REQUEST =>
        val commonMessage = s"El transportista $carrierFullName de la orden $orderDescription ha pedido un auxilio"
        Map(APPLICANT -> (commonMessage, carrierPhoto, true),
          PARTICIPANT -> (commonMessage, carrierPhoto, true),
          CARRIER -> ("Pedido de auxilio realizado con exito", ShippearLogo.logo, false))
    }
  }


  def sendEmail(playersId: List[String], emailType: EventType): Future[OneSignalResponse] = {
    if(active) {
      val email = Email(appId, "Shippear", emailBody(emailType), playersId)

      client.url(NotificationPath)
        .withHttpHeaders(ContentType, Authorization)
        .post(email.toJson)
        .map{response =>

          response.status match {
            case 200 =>

              val body = Try(response.body.parseJsonTo[OneSignalResponse])
                .recover { case _: MismatchedInputException =>
                  info("Failed with serialization to OneSignalResponse, trying with InvalidPlayersIds...")
                  response.body.parseJsonTo[InvalidPlayerIds]}
                .recover { case _: MismatchedInputException =>
                  info("Failed with serialization to InvalidPLayersIds, trying with NoSubscribedPlayers...")
                  response.body.parseJsonTo[NoSubscribedPlayers]
                }

              body match {
                case Success(OneSignalResponse(id,recipients,None)) => OneSignalResponse(id, recipients, None)
                case Success(OneSignalResponse(_,_,Some(errors))) => throw ShippearException(NotificationException, errors.mkString(", "))
                case Success(InvalidPlayerIds(_, _, errors)) =>  throw ShippearException(NotificationException, errors.mkString(", "))
                case Success(NoSubscribedPlayers(_, _, errors)) =>  throw ShippearException(NotificationException, errors.mkString(", "))
                case _ => throw ShippearException(NotificationException, "Failure in serialization or sending notification")

              }
            case _ => val errors = response.body.parseJsonTo[OneSignalError].errors
              OneSignalResponse(s"Error status: ${response.status}", 0, errors)
          }
        }
    } else
      Future(OneSignalResponse("Emails Deactivated!", 0, None))
  }

  def sendDirectNotification(user: User, order: Order, message: String, silent: Boolean): Future[OneSignalResponse] = {
    if (active) {
      val notification = Notification(appId, List(user.onesignalId),
        Map("en" -> message),
        DataNotification(order._id, order.state, user.photoUrl, RELOAD, silent))

      doNotification(notification)
    }
    else
      Future(OneSignalResponse("Notifications Deactivated!", 0, None))

  }

  def sendFlowMulticastNotification(order: Order, eventType: EventType, userCancelledType : Option[UserType] = None): Future[OneSignalResponse] = {
    if(active) {
      val messagesValue = messageFactory(order, eventType, userCancelledType)

      val applicantNotification = messagesValue.get(APPLICANT).map{ case (message, photo, silent) =>
          Notification(appId, List(order.applicant.oneSignalId),
          Map("en" -> message),
          DataNotification(order._id, order.state, photo, RELOAD, silent))}


      val participantNotification = messagesValue.get(PARTICIPANT).map{
       case(message, photo, silent) =>
          Notification(appId, List(order.participant.oneSignalId),
            Map("en" -> message),
            DataNotification(order._id, order.state, photo, RELOAD, silent))
      }

      val carrierNotifications = order.carrier.flatMap{
        carrier => messagesValue.get(CARRIER).map { case (message, photo, silent) =>
          Notification(appId, List(carrier.oneSignalId),
            Map("en" -> message),
            DataNotification(order._id, order.state, photo, RELOAD, silent))}
      }

      val notifications: List[Notification] = List(participantNotification, carrierNotifications, applicantNotification).flatten

      notifications.map(doNotification)

      Future(OneSignalResponse("", 3, None))

    } else
      Future(OneSignalResponse("Notifications Deactivated!", 0, None))
  }

  def sendMulticastNotification(message: String, order: Order, users: Seq[User]) = {
    if(active) {
      doNotification(Notification(appId, users.map(_.onesignalId).toList,
        Map("en" -> message),
        DataNotification(order._id, order.state, ShippearLogo.logo, RELOAD, silent = false))
      )
    } else
      Future(OneSignalResponse("Notifications Deactivated!", 0, None))
  }

  def doNotification(notification: Notification): Future[OneSignalResponse] = {
    val jsonBody = notification.toJson
    info(s"Sending through push notification the following json: $jsonBody")
    client.url(NotificationPath)
      .withHttpHeaders(ContentType, Authorization)
      .post(jsonBody)
      .map { response =>
        response.status match {
          case 200 =>
            info(s"Body of OneSignalResponse is ${response.body}")
            val body = Try(response.body.parseJsonTo[OneSignalResponse])
              .recover { case _: MismatchedInputException =>
                info("Failed with serialization to OneSignalResponse, trying with InvalidPlayersIds...")
                response.body.parseJsonTo[InvalidPlayerIds]
              }
              .recover { case _: MismatchedInputException =>
                info("Failed with serialization to InvalidPLayersIds, trying with NoSubscribedPlayers...")
                response.body.parseJsonTo[NoSubscribedPlayers]
              }

            body match {
              case Success(OneSignalResponse(id, recipients, None)) => OneSignalResponse(id, recipients, None)
              case Success(OneSignalResponse(_, _, Some(errors))) => throw ShippearException(NotificationException, errors.mkString(", "))
              case Success(InvalidPlayerIds(_, _, errors)) => throw ShippearException(NotificationException, errors.mkString(", "))
              case Success(NoSubscribedPlayers(_, _, errors)) => throw ShippearException(NotificationException, errors.mkString(", "))
              case _ => OneSignalResponse("", 0, None)

            }
          case _ => val errors = response.body.parseJsonTo[OneSignalError].errors
            OneSignalResponse(s"Error status: ${response.status}", 0, errors)
        }
      }
  }


  def device(playerOneSignalId: Option[String]): Future[List[String]] = {
    if(active){
      playerOneSignalId match {
        case Some(id) =>
          client.url(viewDevice(id)).withHttpHeaders(ContentType, Authorization).get.map {
            response =>
              val res: Player = response.body.parseJsonTo[Player]
              List(res.identifier)
          }
        case _ =>
          client.url(viewDevices).withHttpHeaders(ContentType, Authorization).get.map {
            response =>
              val res = response.body.parseJsonTo[Device]
              res.players.map(_.identifier)
          }
      }
    } else
      Future(List())
  }
}