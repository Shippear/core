package model.internal

import java.util.Date

import com.fasterxml.jackson.annotation.JsonProperty

case class Order(@JsonProperty("_id")_id: String,
                 applicant: UserDataOrder,
                 participant: UserDataOrder,
                 carrier: Option[UserDataOrder],
                 description: String,
                 state: String,
                 operationType: String,
                 route: Route,
                 availableFrom: Date,
                 availableTo: Date,
                 awaitFrom: Option[Date],
                 awaitTo: Option[Date],
                 qrCode: Option[Array[Byte]],
                 ratedCarrier: Option[Boolean])
