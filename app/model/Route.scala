package model

import play.api.libs.json.Json

case class Route(origin: Address, destination: Address)

object Route {
  implicit val jsonFormat = Json.writes[Route]
}