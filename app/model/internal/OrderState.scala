package model.internal

import com.fasterxml.jackson.core.`type`.TypeReference

object OrderState extends Enumeration {
  type OrderState = Value

  val PENDING_PARTICIPANT, PENDING_CARRIER, PENDING_PICKUP, ON_TRAVEL, DELIVERED, CANCELLED = Value

  implicit def toString(state: OrderState) = state.toString

  implicit def toState(state: String): OrderState = withName(state.toUpperCase)
}

//For jackson deserialization
class OrderStateType extends TypeReference[OrderState.type]
