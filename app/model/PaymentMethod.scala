package model

import play.api.libs.json.Json

case class PaymentMethod(cardOwnerName: String,
                         cardNumber: String,
                         expirationDate: String,
                         securityCode: String,
                         cardType: String,
                         cbu: Option[String])

object PaymentMethod {
  implicit val jsonFormat = Json.writes[PaymentMethod]
}