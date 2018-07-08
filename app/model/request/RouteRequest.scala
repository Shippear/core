package model.request

import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import model.internal.Geolocation
import model.internal.price.enum.{Size, SizeType, Weight, WeightType}

case class RouteRequest(userName: String,
                        geolocationOrigin: Geolocation,
                        @JsonScalaEnumeration(classOf[SizeType]) size: Size.Size,
                        @JsonScalaEnumeration(classOf[WeightType]) weight: Weight.Weight)