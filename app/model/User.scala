package model

import com.fasterxml.jackson.annotation.JsonProperty
import play.api.libs.json._


case class User(@JsonProperty("_id") _id : String,
                onesignalId: String,
                userName: String,
                firstName: String,
                lastName: String,
                dni: String,
                contactInfo: ContactInfo,
                photoUrl: String,
                addresses: Seq[Address],
                orders: Option[Seq[Order]],
                paymentMethods: Seq[PaymentMethod],
                scoring: Option[Double],
                transport: Option[Transport])

object User {
  implicit val jsonFormat = Json.writes[User]
}