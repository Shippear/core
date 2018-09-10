package notification.email

import com.sendgrid._
import common.{ConfigReader, Logging}
import model.internal.{Order, User, UserDataOrder}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import notification.common.EventType.{EventType, ORDER_CANCELED, ORDER_CREATED, ORDER_FINALIZED, ORDER_ON_WAY, ORDER_WITH_CARRIER, CONFIRM_PARTICIPANT}
import notification.email.HTML._

import scala.util.{Failure, Success, Try}

class EmailClient extends ConfigReader with Logging {

  private val config = envConfiguration.getConfig("email-notification").as[EmailNotificationConfig]
  private var active = config.activated

  private val key = config.key.getOrElse("")

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
      case CONFIRM_PARTICIPANT => CONFIRMED_PARTICIPANT
      case ORDER_WITH_CARRIER => WITH_CARRIER
      case ORDER_ON_WAY => TRAVELLING
      case ORDER_CANCELED => CANCELED
      case ORDER_FINALIZED => FINALIZED
    }
  }

  def createEmail(eventType: EventType, order: Order) = {
    if(active) {

      val req = request(parseBody(eventType, order))

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

  def parseBody(eventType: EventType, order: Order) = {

    val applicant = order.applicant
    val participant = order.participant
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

      case CONFIRM_PARTICIPANT =>
        val toApplicant = new Email(applicant.contactInfo.email, applicant.firstName)
        val toParticipant = new Email(participant.contactInfo.email, participant.firstName)

        val applicantPersonalization = new PersonalizationWrapper()
        applicantPersonalization.addTo(toApplicant)
        applicantPersonalization.addDynamicTemplateData("name", applicant.firstName)
        applicantPersonalization.addDynamicTemplateData("description", order.description)
        applicantPersonalization.addDynamicTemplateData("orderNumber", order.orderNumber.toString)
        applicantPersonalization.addDynamicTemplateData("participantName", participant.firstName)

        val participantPersonalization = new PersonalizationWrapper()
        participantPersonalization.addTo(toParticipant)
        participantPersonalization.addDynamicTemplateData("name", participant.firstName)
        participantPersonalization.addDynamicTemplateData("description", order.description)
        participantPersonalization.addDynamicTemplateData("orderNumber", order.orderNumber.toString)
        participantPersonalization.addDynamicTemplateData("participantName", participant.firstName)

        mail.addPersonalization(applicantPersonalization)
        mail.addPersonalization(participantPersonalization)

      case ORDER_WITH_CARRIER =>
        val toApplicant = new Email(applicant.contactInfo.email, applicant.firstName)
        val toParticipant = new Email(participant.contactInfo.email, participant.firstName)

        val applicantPersonalization = new PersonalizationWrapper()
        applicantPersonalization.addTo(toApplicant)
        applicantPersonalization.addDynamicTemplateData("name", applicant.firstName)
        applicantPersonalization.addDynamicTemplateData("description", order.description)
        applicantPersonalization.addDynamicTemplateData("orderNumber", order.orderNumber.toString)
        applicantPersonalization.addDynamicTemplateData("image", order.qrCodeUrl.get)

        val participantPersonalization = new PersonalizationWrapper()
        participantPersonalization.addTo(toParticipant)
        participantPersonalization.addDynamicTemplateData("name", participant.firstName)
        participantPersonalization.addDynamicTemplateData("description", order.description)
        participantPersonalization.addDynamicTemplateData("orderNumber", order.orderNumber.toString)
        participantPersonalization.addDynamicTemplateData("image", order.qrCodeUrl.get)

        mail.addPersonalization(applicantPersonalization)
        mail.addPersonalization(participantPersonalization)

      case ORDER_ON_WAY =>
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


      case ORDER_FINALIZED =>
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

      case ORDER_CANCELED =>
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
