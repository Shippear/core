package onesignal

case class OneSignalResponse(id: String, recipients: Int, errors: Option[List[String]])

case class OneSignalError(errors: Option[List[String]])

