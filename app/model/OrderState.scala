package model

object OrderState extends Enumeration {
  type OrderState = Value

  val NEW, PENDING_CARRIER, PENDING_PICKUP, ON_TRAVEL, CLOSED, CANCELLED = Value

  implicit def toString(state: OrderState) = state.toString

  implicit def toState(state: String): OrderState = withName(state.toUpperCase)
}
