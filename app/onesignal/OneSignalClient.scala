package onesignal

import com.google.inject.Inject
import common.serialization.{SnakeCaseJsonProtocol, _}
import common.{ConfigReader, Logging}
import model.internal.UserType.UserType
import model.internal.{Order, UserType}
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

  private val ContentType = ("Content-Type", "application/json;charset=utf-8")
  private val Authorization = ("Authorization", s"Basic ${config.auth.getOrElse("")}")

  //Paths Rest API One Signal
  val BasePath = "https://onesignal.com/api/v1"
  val NotificationPath = s"$BasePath/notifications"
  val AddDevice = s"$BasePath/players"
  val viewDevices = s"$AddDevice?app_id=${config.id.getOrElse("")}"
  def viewDevice(id: String) = s"$AddDevice/$id?app_id=${config.id.getOrElse("")}"

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
  private def messageFactory(order : Order, eventType : EventType, userCancelledType : Option[UserType]): Map[String,String] = {

    eventType match {
      case ORDER_CREATED => Map(UserType.PARTICIPANT.toString ->
        s"${order.applicant.firstName} ${order.applicant.lastName} quiere que seas participante de: ${order.description}" )

      case ORDER_WITH_CARRIER =>Map(UserType.PARTICIPANT.toString ->
        s"${order.carrier.get.firstName} ${order.carrier.get.lastName} sera el transportista de : ${order.description}" ,
        UserType.APPLICANT.toString ->
        s"${order.carrier.get.firstName} ${order.carrier.get.lastName} sera el transportista de la solicitud que participas con ${order.participant.firstName} ${order.participant.lastName}")

      case CONFIRM_PARTICIPANT => Map(UserType.APPLICANT.toString ->
        s"${order.participant.firstName} ${order.participant.lastName} ha aceptado tu solicitud" )

      case ORDER_ON_WAY =>
        val messageFromCarrier  =  s"${order.carrier.get.firstName} ${order.carrier.get.lastName} ha retirado el objeto ${order.description} y se encuentra en camino"
        Map(UserType.APPLICANT.toString -> messageFromCarrier,
          UserType.PARTICIPANT.toString ->messageFromCarrier )

      case ORDER_CANCELED => val usersToSend  = UserType.values.filterNot(x=>x.toString == userCancelledType.toString)

        usersToSend.map { u =>
          u.toString -> s"fue cancelado por ${order.applicant.firstName} ${order.applicant.lastName}"
        }.toMap

      case ORDER_FINALIZED =>
        val messageFromCarrier  = s"La solicitud de ${order.description} se ha completado con exito."
        Map(UserType.APPLICANT.toString -> messageFromCarrier,
          UserType.PARTICIPANT.toString ->messageFromCarrier )
    }
  }


  def sendEmail(playersId: List[String], emailType: EventType): Future[OneSignalResponse] = {
    if(active) {
      val email = Email(config.id.getOrElse(""), "Shippear", emailBody(emailType), playersId)

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
      val messagesValue =   messageFactory(order, eventType, userCancelledType)

      val participantNotifications =
        Notification(config.id.getOrElse(""), List(order.applicant.id),
        Map( "en" -> messagesValue(UserType.PARTICIPANT.toString)),
        DataNotification(order._id, order.state, order.participant.photoUrl, ActionState.RELOADED))

      val carrierNotifications = Notification(config.id.getOrElse(""), List(order.applicant.id),
        Map("en" -> messagesValue(UserType.CARRIER.toString)),
        DataNotification(order._id, order.state, order.carrier.get.photoUrl,ActionState.RELOADED))

      val applicantNotifications = Notification(config.id.getOrElse(""), List(order.applicant.id),
        Map("en"->messagesValue(UserType.APPLICANT.toString)),
        DataNotification(order._id, order.state, order.applicant.photoUrl, ActionState.RELOADED))

      val notifications = List(participantNotifications, carrierNotifications, applicantNotifications)

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