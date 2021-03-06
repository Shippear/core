package model.response

import java.util.Date

import com.fasterxml.jackson.annotation.JsonProperty
import model.internal._

case class UserResponse(@JsonProperty("_id") _id : String,
                        appType: String,
                        onesignalId: String,
                        userName: String,
                        firstName: String,
                        lastName: String,
                        dni: String,
                        birthDate: Date,
                        contactInfo: ContactInfo,
                        photoUrl: String,
                        addresses: Seq[Address],
                        ordersInProgress: Option[Seq[Order]],
                        ordersFinalized: Option[Seq[Order]],
                        ordersToBeConfirmed: Option[Seq[Order]],
                        paymentMethods: Option[Seq[PaymentMethod]],
                        cbu: Option[String],
                        scoring: Option[Float],
                        transport: Option[Seq[Transport]])
