package onesignal

case class Email(appId: String, emailSubject: String,
                 templateId: String, includePlayerIds: List[String])

