package model

import play.api.libs.json.Json

case class Address(geolocation: Geolocation,
                   alias: String,
                   street: String,
                   number: Int,
                   zipCode: String,
                   appartament: Option[String],
                   localityId: Int)

object Address {
  implicit val jsonFormat = Json.writes[Address]
}