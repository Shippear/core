package model.response

import java.util.Date

import com.fasterxml.jackson.annotation.JsonProperty
import model.internal._

case class UserResponse(@JsonProperty("_id") _id : String,
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
                         paymentMethods: Seq[PaymentMethod],
                         cbu: Option[String],
                         scoring: Option[Double],
                         transport: Option[Seq[Transport]])
