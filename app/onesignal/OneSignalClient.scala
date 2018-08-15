package onesignal

import com.google.inject.Inject
import common.serialization.{SnakeCaseJsonProtocol, _}
import common.{ConfigReader, Logging}
import model.internal.{Order, UserType}
import model.internal.UserType._
import onesignal.ActionState._
import model.internal.UserType.{APPLICANT, CARRIER, PARTICIPANT}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import onesignal.EventType.{EventType, _}
import onesignal.HTML._
import play.api.libs.ws.WSClient
import service.Exception.ShippearException

import scala.concurrent.{ExecutionContext, Future}

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

  def messageFactory(order: Order, eventType: EventType, userCancelledType: Option[UserType]): Map[UserType,String] = {

    val applicantFullName = s"${order.applicant.firstName} ${order.applicant.lastName}"
    val participantFullName = s"${order.participant.firstName} ${order.participant.lastName}"
    val carrierFullName = order.carrier.map{carrier => s"${carrier.firstName} ${carrier.lastName}"}.getOrElse("")
    val orderDescription = order.description

    val cancelledFullName = userCancelledType match {
      case Some(APPLICANT) => applicantFullName
      case Some(PARTICIPANT) => participantFullName
      case Some(CARRIER) => carrierFullName
      case _ => throw ShippearException("User cancelled type not found!")
    }

    eventType match {
      //Order created
      case ORDER_CREATED => Map(PARTICIPANT ->
       s"$participantFullName quiere que seas participante de: $orderDescription")

      //Confirmed Participant
      case CONFIRM_PARTICIPANT => Map(APPLICANT ->
        s"$applicantFullName ha aceptado tu solicitud")

      //Confirmed Carrier
      case ORDER_WITH_CARRIER =>
        Map(PARTICIPANT ->
        s"$carrierFullName sera el transportista de: $orderDescription" ,
            APPLICANT ->
        s"$carrierFullName sera el transportista de la solicitud que participas con $participantFullName de: $orderDescription")

      //Carrier validated by QR
      case ORDER_ON_WAY =>
        val messageFromCarrier =  s"$carrierFullName ha retirado el objeto $orderDescription y se encuentra en camino!"
        Map(APPLICANT -> messageFromCarrier,
          PARTICIPANT -> messageFromCarrier)

      //Canceled by some user
      case ORDER_CANCELED =>
        val usersToSend = UserType.values.filterNot(_.equals(userCancelledType))

        usersToSend.map { u =>
          u -> s"fue cancelado por $cancelledFullName"
        }.toMap

      case ORDER_FINALIZED =>
        val messageFromCarrier  = s"La solicitud de $orderDescription se ha completado con exito!"
        Map(APPLICANT -> messageFromCarrier, PARTICIPANT -> messageFromCarrier, CARRIER -> messageFromCarrier)
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
              val body = response.body.parseJsonTo[OneSignalResponse]
              body.errors match {
                case Some(errors) => throw ShippearException(errors.mkString(", "))
                case _ => body
              }
            case _ => val errors = response.body.parseJsonTo[OneSignalError].errors
              OneSignalResponse(s"Error status: ${response.status}", 0, errors)
          }
        }
    } else
      Future(OneSignalResponse("Emails Deactivated!", 0, None))
  }

  def sendNotification(order: Order, eventType: EventType, userCancelledType : Option[UserType] = None): Future[OneSignalResponse] = {
    if(active) {
      val messagesValue = messageFactory(order, eventType, userCancelledType)

      val applicantNotification = messagesValue.get(APPLICANT).map{message =>
          Notification(appId, List(order.applicant.id),
          Map("en" -> message),
          DataNotification(order._id, order.state, order.applicant.photoUrl, RELOADED))}


      val participantNotification = messagesValue.get(PARTICIPANT).map{
        message =>
          Notification(appId, List(order.applicant.id),
            Map("en" -> message),
            DataNotification(order._id, order.state, order.participant.photoUrl, RELOADED))
      }

      val carrierNotifications = order.carrier.flatMap{
        carrier => messagesValue.get(CARRIER).map { message =>
          Notification(appId, List(order.applicant.id),
            Map("en" -> message),
            DataNotification(order._id, order.state, carrier.photoUrl, RELOADED))}
      }

      val notifications: List[Option[Notification]] = List(participantNotification, carrierNotifications, applicantNotification)

      notifications.map{ notification =>
        client.url(NotificationPath)
          .withHttpHeaders(ContentType, Authorization)
          .post(notification.toJson)
          .map{response =>
            response.status match {
              case 200 =>
                val body = response.body.parseJsonTo[OneSignalResponse]
                body.errors match {
                  case Some(errors) => throw ShippearException(errors.mkString(", "))
                  case _ => body
                }
              case _ => val errors = response.body.parseJsonTo[OneSignalError].errors
                OneSignalResponse(s"Error status: ${response.status}", 0, errors)
            }
          }
      }

      Future(OneSignalResponse("", 3, None))

    } else
      Future(OneSignalResponse("Emails Deactivated!", 0, None))
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