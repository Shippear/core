package model.internal.price.enum

import com.fasterxml.jackson.core.`type`.TypeReference

object Weight extends Enumeration {
  type Weight = Value

  val LIGHT, MEDIUM, HEAVY = Value

  implicit def toString(weight: Weight): String = weight.toString

  implicit def toWeight(weight: String): Weight = withName(weight.toUpperCase)
}


class WeightType extends TypeReference[Weight.type]
