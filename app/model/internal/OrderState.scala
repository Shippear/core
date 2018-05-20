package model.internal

object OrderState extends Enumeration {
  type OrderState = Value

  val PENDING_PARTICIPANT, PENDING_CARRIER, PENDING_PICKUP, ON_TRAVEL, DELIVERED, CANCELLED = Value

  implicit def toString(state: OrderState) = state.toString

  implicit def toState(state: String): OrderState = withName(state.toUpperCase)
}
