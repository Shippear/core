package model.internal

import play.api.libs.json.Json

case class Transport(transportType: String, domain: Option[String], model: Option[String])


object Transport {
  implicit val jsonFormat = Json.writes[Transport]
}