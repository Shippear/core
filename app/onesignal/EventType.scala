package onesignal

object EventType extends Enumeration {
  type EventType = Value
  val ORDER_CREATED, CONFIRM_PARTICIPANT, ORDER_WITH_CARRIER, ORDER_ON_WAY, ORDER_CANCELED, ORDER_FINALIZED = Value

  implicit def toString(state: EventType) = state.toString

  implicit def toState(state: String): EventType = withName(state.toUpperCase)
}
