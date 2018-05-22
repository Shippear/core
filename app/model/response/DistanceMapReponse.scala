package model.response
import play.api.libs.json.Json
case class DistanceMapReponse (destinationAddreses:String,originAddresses:String)


object DistanceMapReponse {
  implicit val jsonFormat = Json.writes[DistanceMapReponse]
}