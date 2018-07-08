package model.internal.price.enum

import com.fasterxml.jackson.core.`type`.TypeReference

object Size extends Enumeration {
  type Size = Value

  val SMALL, MEDIUM, BIG = Value

  implicit def toString(size: Size): String = size.toString

  implicit def toSize(size: String): Size = withName(size.toUpperCase)
}

class SizeType extends TypeReference[Size.type]