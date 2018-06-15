package service

import com.google.inject.Inject
import onesignal.OneSignalClient

class MailService @Inject()(mailClient: OneSignalClient) {

  def activateEmail(status: Boolean) = s"Mailing status: ${mailClient.activated(status)}"

  def sendEmail(oneSignal: String, typeMail: String) = mailClient.sendEmail(List(oneSignal), typeMail)

  def device(userOneSignalId: Option[String]) = mailClient.device(userOneSignalId)

}
