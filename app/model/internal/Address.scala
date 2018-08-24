package model.internal

import java.util.Date

case class Address(geolocation: Geolocation,
                   alias: Option[String],
                   street: String,
                   number: Int,
                   zipCode: String,
                   apartment: Option[String],
                   city: City,
                   public: Boolean,
                   awaitFrom: Option[Date],
                   awaitTo: Option[Date])


case class MinimalAddress(geolocation: Geolocation, street: String)