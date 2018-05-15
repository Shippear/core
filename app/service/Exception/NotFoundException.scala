package service.Exception

case class NotFoundException(message: String)
  extends RuntimeException(message)
