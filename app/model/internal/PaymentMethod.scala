package model.internal

case class PaymentMethod(cardOwnerName: String,
                         cardNumber: String,
                         expirationDate: String,
                         securityCode: String,
                         cardType: String)