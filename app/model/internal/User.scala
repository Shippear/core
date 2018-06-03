package model.internal

import com.fasterxml.jackson.annotation.JsonProperty
import model.internal.OrderState.{CANCELLED, DELIVERED, ON_TRAVEL, PENDING_CARRIER, PENDING_PARTICIPANT, PENDING_PICKUP}
import model.response.UserResponse
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
                cbu: Option[String],
                scoring: Option[Double],
                transport: Option[Seq[Transport]])

object User {
  implicit val jsonFormat = Json.writes[User]

  val toBeConfirmedStates: List[String] = List(PENDING_PARTICIPANT, PENDING_CARRIER)
  val inProgressStates: List[String] = List(PENDING_PICKUP, ON_TRAVEL)
  val finalizedStates: List[String] = List(CANCELLED, DELIVERED)

  implicit def user2Response(user: User): UserResponse = {
    val finalized = user.orders.map(o => o.filter { order => finalizedStates.contains(order.state) })
    val inProgress = user.orders.map(o => o.filter { order => inProgressStates.contains(order.state) })
    val toBeConfirmed = user.orders.map(o => o.filter { order => toBeConfirmedStates.contains(order.state) })

    UserResponse(user._id,
      user.onesignalId,
      user.userName,
      user.firstName,
      user.lastName,
      user.dni,
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