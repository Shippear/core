package model.internal

import java.util.Date

import com.fasterxml.jackson.annotation.JsonProperty
import model.internal.OrderState._
import model.response.UserResponse


case class User(@JsonProperty("_id") _id : String,
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
                orders: Option[Seq[Order]],
                paymentMethods: Option[Seq[PaymentMethod]],
                cbu: Option[String],
                scoring: Option[Float],
                transport: Option[Seq[Transport]])

object User {
  val toBeConfirmedStates: List[String] = List(PENDING_PARTICIPANT, PENDING_CARRIER)
  val inProgressStates: List[String] = List(PENDING_PICKUP, ON_TRAVEL, PENDING_AUX)
  val finalizedStates: List[String] = List(CANCELLED, DELIVERED)

  implicit def user2Response(user: User): UserResponse = {
    val finalized = user.orders.map(o => o.filter { order => finalizedStates.contains(order.state) })
    val inProgress = user.orders.map(o => o.filter { order => inProgressStates.contains(order.state) })
    val toBeConfirmed = user.orders.map(o => o.filter { order => toBeConfirmedStates.contains(order.state) })

    UserResponse(user._id,
      user.appType,
      user.onesignalId,
      user.userName,
      user.firstName,
      user.lastName,
      user.dni,
      user.birthDate,
      user.contactInfo,
      user.photoUrl,
      user.addresses,
      inProgress,
      finalized,
      toBeConfirmed,
      user.paymentMethods,
      user.cbu,
      user.scoring,
      user.transport)

  }
}