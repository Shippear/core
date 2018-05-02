package model

import play.api.libs.json.Json

case class ContactInfo(email: String, phone: String)

object ContactInfo {
  implicit val jsonFormat = Json.format[ContactInfo]
}
