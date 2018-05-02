package model

import play.api.libs.json.Json

case class Geolocation(latitude: Double, longitude: Double)

object Geolocation {
  implicit val jsonFormat = Json.writes[Geolocation]
}