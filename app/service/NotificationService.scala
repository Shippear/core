package service

import com.google.inject.Inject
import model.internal.Order
import model.internal.UserType.UserType
import notification.email.EmailClient
import notification.pushnotification.PushNotificationClient

class NotificationService @Inject()(pushNotificationClient: PushNotificationClient,
                                    emailClient: EmailClient) {

  def activateMail(status: Boolean) =
    s"Mailing status: ${emailClient.activated(status)}"

  def activatePush(status: Boolean) =
    s"Push status: ${pushNotificationClient.activated(status)}"

}
