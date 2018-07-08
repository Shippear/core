package model.internal.price.enum

import com.fasterxml.jackson.core.`type`.TypeReference

object Distance extends Enumeration {
  type Distance = Value

  val SHORT, MEDIUM, LONG = Value

  implicit def toString(distance: Distance): String = distance.toString

  implicit def toDistance(distance: String): Distance = withName(distance.toUpperCase)
}

class DistanceType extends TypeReference[Distance.type]