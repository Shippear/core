package notification.pushnotification

case class OneSignalResponse(id: String, recipients: Int, errors: Option[Seq[String]])

case class InvalidPlayerIds(id: String, recipients: Int, invalidPlayerIds: Seq[String])

case class NoSubscribedPlayers(id: String, recipients: Int, errors: Seq[String])

case class OneSignalError(errors: Option[Seq[String]])


