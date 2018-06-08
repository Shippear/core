package model.internal

case class Address(geolocation: Geolocation,
                   alias: Option[String],
                   street: String,
                   number: Int,
                   zipCode: String,
                   appartament: Option[String],
                   city: City,
                   public: Boolean)