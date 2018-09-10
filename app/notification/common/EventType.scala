package notification.common

object EventType extends Enumeration {
  type EventType = Value
  val ORDER_CREATED, CONFIRM_PARTICIPANT, ORDER_WITH_CARRIER, ORDER_ON_WAY, ORDER_CANCELED, ORDER_FINALIZED, AUX_REQUEST = Value

  implicit def toString(event: EventType) = event.toString

  implicit def toEvent(event: String): EventType = withName(event.toUpperCase)
}
