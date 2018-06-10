package model.response
import model.internal.Geolocation
import play.api.libs.json.Json
case class DistanceMapResponse(originAddresses:Geolocation, destinationAddreses:Geolocation, distance:String, duration:String)


object DistanceMapResponse {
  implicit val jsonFormat = Json.writes[DistanceMapResponse]
}