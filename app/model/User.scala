package model

import com.fasterxml.jackson.annotation.JsonProperty
import play.api.libs.json.Json


case class User(@JsonProperty("_id") _id : String,
                onesignalId: String,
                userName: String,
                firstName: String,
                lastName: String,
                dni: String,
                contactInfo: ContactInfo,
                photoUrl: String,
                addresses: Seq[Address],
                order: Option[Order],
                paymentMethods: Seq[PaymentMethod])

object User {
  implicit val jsonFormat = Json.writes[User]
}