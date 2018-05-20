package model.internal

import play.api.libs.json.Json

case class Address(geolocation: Geolocation,
                   alias: Option[String],
                   street: String,
                   number: Int,
                   zipCode: String,
                   appartament: Option[String],
                   localityId: Int,
                   public: Boolean)

object Address {
  implicit val jsonFormat = Json.writes[Address]
}