package model.internal

import com.fasterxml.jackson.annotation.JsonProperty
import play.api.libs.json.Json

case class CacheGeolocation(@JsonProperty("_id") _id: String, geolocation: Geolocation)

object CacheGeolocation {
  implicit val jsonFormat = Json.writes[CacheGeolocation]
}