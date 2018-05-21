package service.Exception

case class InternalServerErrorException(message: String, throwable: Throwable = null) extends RuntimeException(message, throwable)
