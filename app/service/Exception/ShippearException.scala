package service.Exception

case class ShippearException(message: String)
  extends RuntimeException(message)