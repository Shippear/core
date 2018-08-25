package service

import com.google.inject.Inject
import model.internal.Order
import model.internal.UserType.UserType
import onesignal.OneSignalClient

class MailService @Inject()(mailClient: OneSignalClient) {

  def activateEmail(status: Boolean) = s"Mailing status: ${mailClient.activated(status)}"

  def sendEmail(oneSignal: String, typeMail: String) =
    mailClient.sendEmail(List(oneSignal), typeMail)

  def sendNotification(order: Order, eventType: String, userCancelledType : Option[UserType])=
    mailClient.sendFlowMulticastNotification(order, eventType, userCancelledType)

  def device(userOneSignalId: Option[String]) = mailClient.device(userOneSignalId)

}
