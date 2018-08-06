package model.internal

import com.fasterxml.jackson.core.`type`.TypeReference
import model.internal.TransportType.TransportType

case class Transport(transportType: TransportType, domain: Option[String], model: Option[String])

object TransportType extends Enumeration {
  type TransportType = Value

  val WALKING, BICYCLE, MOTORIZED = Value

  implicit def toString(transport: TransportType): String = transport.toString

  implicit def toTransport(transport: String): TransportType = withName(transport.toUpperCase)
}

class TransportTypeType extends TypeReference[TransportType.type]