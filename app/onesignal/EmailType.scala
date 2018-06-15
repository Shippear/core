package onesignal

object EmailType extends Enumeration {
  type EmailType = Value
  val ORDER_CREATED, ORDER_WITH_CARRIER, ORDER_ON_WAY, ORDER_CANCELED, ORDER_FINALIZED = Value

  implicit def toString(state: EmailType) = state.toString

  implicit def toState(state: String): EmailType = withName(state.toUpperCase)
}
