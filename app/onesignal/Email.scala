package onesignal

case class Email(appId: String, emailSubject: String,
                 emailBody: String, includePlayerIds: List[String])

