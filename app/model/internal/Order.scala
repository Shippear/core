package model.internal

import java.util.Date

import com.fasterxml.jackson.annotation.JsonProperty

case class Order(@JsonProperty("_id")_id: String,
                 applicant: UserDataOrder,
                 participant: UserDataOrder,
                 carrier: Option[UserDataOrder],
                 historicCarriers: Option[List[UserDataOrder]],
                 orderNumber: Long,
                 description: String,
                 state: String,
                 operationType: String,
                 size: String,
                 weight: String,
                 supportedTransports: List[String],
                 route: Route,
                 availableFrom: Date,
                 availableTo: Date,
                 timeoutTime: Option[Date],
                 qrCode: Option[Array[Byte]],
                 ratedCarrier: Option[Boolean],
                 ratedValue: Option[Int],
                 paymentMethod: PaymentMethod,
                 price: Double,
                 carrierEarning: Option[Double],
                 finalizedDate: Option[Date])
