package service

import com.google.inject.Inject
import notification.email.{CloudinaryWrapper, EmailClient}
import notification.pushnotification.PushNotificationClient

class NotificationService @Inject()(pushNotificationClient: PushNotificationClient,
                                    emailClient: EmailClient,
                                    cloudinary: CloudinaryWrapper) {

  def activateMail(status: Boolean) =
    s"Mailing status: ${emailClient.activated(status)}"

  def activatePush(status: Boolean) =
    s"Push status: ${pushNotificationClient.activated(status)}"

  def activateImageUpload(status: Boolean) = {
    s"Images upload status: ${cloudinary.activated(status)}"
  }

}
