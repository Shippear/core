package model

case class Address(geolocation: Geolocation,
                   alias: String,
                   street: String,
                   number: Int,
                   zipCode: String,
                   appartament: Option[String],
                   localityId: Int)
