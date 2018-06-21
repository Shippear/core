package service.Exception

case class InvalidAddressException(message: String)
  extends RuntimeException(message)