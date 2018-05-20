package model.internal

import play.api.libs.json.Json

case class City(id: Int, name: String)

object City {
  implicit val jsonFormat = Json.writes[City]
}
