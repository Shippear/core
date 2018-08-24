package model.internal

import com.fasterxml.jackson.core.`type`.TypeReference

object AppType extends Enumeration {
  type AppType = Value

  val USER, CARRIER = Value

  implicit def toString(appType: AppType) = appType.toString

  implicit def toAppType(appType: String): AppType = withName(appType.toUpperCase)

}

//For jackson deserialization
class AppTypeType extends TypeReference[AppType.type]
