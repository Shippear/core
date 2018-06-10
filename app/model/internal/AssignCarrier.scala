package model.internal

import play.api.libs.json.Json

case class AssignCarrier (orderId: String,
                           userId: String
                         )

object AssignCarrier {
  implicit val jsonFormat = Json.writes[AssignCarrier]
}
