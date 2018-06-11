package onesignal

import com.google.inject.Inject
import common.serialization.{SnakeCaseJsonProtocol, _}
import common.{ConfigReader, Logging}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import onesignal.EmailType.{EmailType, _}
import play.api.libs.ws.WSClient
import HTML._

import scala.concurrent.{ExecutionContext, Future}

case class OneSignalConfig(id: Option[String], auth: Option[String])

class OneSignalClient @Inject()(client: WSClient)(implicit ec: ExecutionContext) extends ConfigReader with Logging with SnakeCaseJsonProtocol {

  val config = envConfiguration.getConfig("email-notification").as[OneSignalConfig]
  val activated = config.id.isDefined

  private val ContentType = ("Content-Type", "application/json;charset=utf-8")
  private val Authorization = ("Authorization", s"Basic ${config.auth.getOrElse("")}")

  //Paths Rest API One Signal
  val OneSignalPath = "https://onesignal.com/api/v1"
  val NotificationPath = s"$OneSignalPath/notifications"
  val AddDevice = s"$OneSignalPath/players"
  val viewDevices = s"$AddDevice?app_id=${config.id.getOrElse("")}"
  def viewDevice(id: String) = s"$AddDevice/$id?app_id=${config.id.getOrElse("")}"

  private def emailBody(emailType: EmailType): String = {
    emailType match {
      case ORDER_CREATED => CREATED
      case ORDER_ON_WAY => TRAVELLING
      case ORDER_CANCELED => CANCELED
      case ORDER_FINALIZED => FINALIZED
    }
  }


  def sendEmail(playersId: List[String], emailType: EmailType): Future[EmailResponse] = {
    if(activated) {
      val email = Email(config.id.getOrElse(""), "Shippear", emailBody(emailType), playersId)

      client.url(NotificationPath)
        .withHttpHeaders(ContentType, Authorization)
        .post(email.toJson)
        .map(_.body.parseJsonTo[EmailResponse])
    } else {
      Future(EmailResponse("Emails Deactivated!", 0))
    }

  }

  def device(playerOneSignalId: Option[String]): Future[List[String]] = {
    if(activated){
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
    } else {
      Future(List())
    }

  }
}