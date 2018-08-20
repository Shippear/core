package service.Exception

case class ShippearException(code: Int, message: String) extends RuntimeException(message)