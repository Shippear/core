package notification.email

import com.sendgrid._
import common.{ConfigReader, Logging}
import model.internal.{Order, User}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import notification.email.HTML._
import notification.common.EventType.{EventType, ORDER_CANCELED, ORDER_CREATED, ORDER_FINALIZED, ORDER_ON_WAY, ORDER_WITH_CARRIER}

import scala.util.{Failure, Success, Try}

class EmailClient extends ConfigReader with Logging {

  private val config = envConfiguration.getConfig("email-notification").as[EmailNotificationConfig]
  private var active = config.activated
  private val key = config.key.getOrElse("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")

  private val SendEndpoint = "mail/send"
  private val EmailShippear = "shippear.argentina@gmail.com"
  private val sendGrid = new SendGrid(key)

  def activated(state: Boolean): Boolean = {
    active = state
    active
  }

  private def emailTemplate(emailType: EventType): String = {
    emailType match {
      case ORDER_CREATED => CREATED
      case ORDER_WITH_CARRIER => WITH_CARRIER
      case ORDER_ON_WAY => TRAVELLING
      case ORDER_CANCELED => CANCELED
      case ORDER_FINALIZED => FINALIZED
    }
  }

  def createEmail(eventType: EventType, order: Order, users: List[User]) = {
    if(active) {

      val req = request(parseBody(eventType, order, users))

      Try(sendGrid.api(req)) match {
        case Success(response) => response.getStatusCode
        case Failure(ex: Throwable) => warn(s"Error sending email ${ex.getMessage}")
      }

    }
  }

  def request(body: Mail) = {
    val request = new Request()
    request.setMethod(Method.POST)
    request.setEndpoint(SendEndpoint)
    request.setBody(body.build())
    request
  }

  def parseBody(eventType: EventType, order: Order, users: List[User]) = {

    val applicant = users.find(u => u._id.equals(order.applicant.id)).head
    val participant = users.find(u => u._id.equals(order.participant.id)).head
    val from = new Email(EmailShippear)

    val mail = new Mail()
    val content = new Content("text/html", " ")
    mail.addContent(content)
    mail.setFrom(from)
    eventType match {
      case ORDER_CREATED =>
        val toApplicant = new Email(applicant.contactInfo.email, applicant.firstName)
        val toParticipant = new Email(participant.contactInfo.email, participant.firstName)


        val applicantPersonalization = new PersonalizationWrapper()
        applicantPersonalization.addTo(toApplicant)
        applicantPersonalization.addDynamicTemplateData("name", applicant.firstName)
        applicantPersonalization.addDynamicTemplateData("description", order.description)
        applicantPersonalization.addDynamicTemplateData("orderNumber", order.orderNumber.toString)

        val participantPersonalization = new PersonalizationWrapper()
        participantPersonalization.addTo(toParticipant)
        participantPersonalization.addDynamicTemplateData("name", participant.firstName)
        participantPersonalization.addDynamicTemplateData("description", order.description)
        participantPersonalization.addDynamicTemplateData("orderNumber", order.orderNumber.toString)

        mail.addPersonalization(applicantPersonalization)
        mail.addPersonalization(participantPersonalization)

      case _ => info("not found")

    }

    mail.setTemplateId(emailTemplate(eventType))
    mail

  }



}
