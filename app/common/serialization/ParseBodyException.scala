package common.serialization

case class ParseBodyException(message: String, throwable: Throwable = null) extends RuntimeException(message, throwable)
