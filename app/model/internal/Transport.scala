package model.internal

import com.fasterxml.jackson.core.`type`.TypeReference

case class Transport(transportType: String, domain: Option[String], model: Option[String])

object TransportType extends Enumeration {
  type TransportType = Value

  val WALKING, BICYCLE, MOTORCYCLE, CAR = Value

  implicit def toString(transport: TransportType): String = transport.toString

  implicit def toTransport(transport: String): TransportType = withName(transport.toUpperCase)
}

class TransportTypeType extends TypeReference[TransportType.type]