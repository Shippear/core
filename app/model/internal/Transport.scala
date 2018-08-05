package model.internal

import model.internal.price.enum.Size.{Value, withName}

case class Transport(transportType: String, domain: Option[String], model: Option[String])

object TransportType extends Enumeration {
  type TransportType = Value

  val SMALL, MEDIUM, BIG = Value

  implicit def toString(size: Size): String = size.toString

  implicit def toSize(size: String): Size = withName(size.toUpperCase)
}

class SizeType extends TypeRef
