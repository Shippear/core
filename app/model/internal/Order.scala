package model.internal

import java.util.Date

import play.api.libs.json.Json

case class Order(_id: String,
                 applicantId: String,
                 participantId: String,
                 carrierId: Option[String],
                 state: String,
                 operationType: String,
                 route: Route,
                 availableFrom: Date,
                 availableTo: Date,
                 awaitFrom: Option[Date],
                 awaitTo: Option[Date],
                 qrCode: Option[String])

object Order {
  implicit val jsonFormat = Json.writes[Order]
}