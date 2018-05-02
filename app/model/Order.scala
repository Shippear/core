package model

import play.api.libs.json.Json

case class Order(x: Int, y: Int)

object Order {
  implicit val jsonFormat = Json.writes[Order]
}