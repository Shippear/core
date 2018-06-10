package model.request

import model.internal.Geolocation
import play.api.libs.json.Json

case class RouteRequest (userName:String,geolocationAddress:Geolocation)

object RouteRequest {
  implicit val jsonFormat = Json.writes[RouteRequest]
}