package onesignal

object EmailType extends Enumeration {
  type EmailType = Value
  val ORDER_CREATED, ORDER_ON_WAY, ORDER_CANCELED, ORDER_FINALIZED = Value
}
