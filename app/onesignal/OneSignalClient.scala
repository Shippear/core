package onesignal

import com.google.inject.Inject
import common.{ConfigReader, Logging}
import play.api.libs.json.{Json, Writes}
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.DefaultBodyReadables._
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext
import scala.util.Try

class OneSignalClient @Inject()(client: WSClient)(implicit ec: ExecutionContext) extends ConfigReader with Logging {

  val appId = Try(envConfiguration.getConfig("email-notification").getString("app-id")).toOption
  val auth = Try(envConfiguration.getConfig("email-notification").getString("auth")).toOption
  val ContentType = ("Content-Type", "application/json;charset=utf-8")
  val Authorization = ("Authorization", s"Basic ${auth.getOrElse("")}")

  def createEmail(playersId: List[String]) =
    Email(appId.getOrElse(""),
      "TEST",
      "<html><head>Shippear</head><body><h1>Estado de tu envio<h1><hr/><p>Hi Nick,</p><p>Thanks for subscribing to Cat Facts! We can't wait to surprise you with funny details about your favorite animal.</p><h5>Today's Cat Fact (March 27)</h5><p>In tigers and tabbies, the middle of the tongue is covered in backward-pointing spines, used for breaking off and gripping meat.</p><a href='https://catfac.ts/welcome'>Show me more Cat Facts</a><hr/><p><small>(c) 2018 Cat Facts, inc</small></p><p><small><a href='[unsubscribe_url]'>Unsubscribe</a></small></p></body></html>",
      playersId)

  val NotificationPath = "https://onesignal.com/api/v1/notifications"

  def sendEmail(playersId: List[String]) = {
    appId.map { _ =>

      val email = createEmail(playersId)
      client.url(NotificationPath).withHttpHeaders(ContentType, Authorization).post(Json.stringify(Json.toJson(email)(Email.jsonFormat))).map{
       response =>
         val a = response.json.as[EmailResponse](EmailResponse.jsonFormat)
        // val emailResponse: EmailResponse = response.body[EmailResponse]
         info(s"Status sending mail ${response.status}; recipients: ")
      }
        .recover{ case ex: Exception => error(s" Error ssenm sending mail")}
    }
  }



}

case class Email(app_id: String, email_subject: String,
                 email_body: String, include_player_ids: List[String],
                 email_from_name: String = "Shippear", email_from_address: String = "shippear.argentina@gmail.com")


object Email {
  implicit val jsonFormat = Json.format[Email]
}


case class EmailResponse(id: String, recipients: Int)

object EmailResponse {
  implicit val jsonFormat = Json.format[EmailResponse]
}
