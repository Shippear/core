package model.internal

case class PaymentMethod(cardOwnerName: String,
                         cardNumber: String,
                         cardCode: String,
                         bankCode: String,
                         expirationDate: String,
                         securityCode: String,
                         cardType: String)