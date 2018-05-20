package model.internal

import play.api.libs.json._


case class User(_id : String,
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
                cbu: Option[String],
                scoring: Option[Double],
                transport: Option[Seq[Transport]])

object User {
  implicit val jsonFormat = Json.writes[User]
}