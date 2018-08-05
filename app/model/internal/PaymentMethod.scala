package model.internal

case class PaymentMethod(cardOwnerName: String,
                         cardNumber: String,
                         cardCode: Option[String],
                         bankCode: Option[String],
                         expirationDate: String,
                         securityCode: String,
                         cardType: Option[String])