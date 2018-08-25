package onesignal

case class OneSignalResponse(id: String, recipients: Int, errors: Option[List[String]])

case class InvalidPlayerIds(id: String, recipients: Int, invalidPlayerIds: List[String])

case class NoSubscribedPlayers(id: String, recipients: Int, errors: List[String])

case class OneSignalError(errors: Option[List[String]])
