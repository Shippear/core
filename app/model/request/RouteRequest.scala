package model.request

import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import model.internal.Geolocation
import model.internal.price.enum.{SizeType, WeightType}

case class RouteRequest(userName: String,
                        geolocationOrigin: Geolocation,
                        @JsonScalaEnumeration(classOf[SizeType]) size: String,
                        @JsonScalaEnumeration(classOf[WeightType]) weight: String)