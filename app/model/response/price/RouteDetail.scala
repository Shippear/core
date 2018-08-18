package model.response.price

import model.internal.{Address, Geolocation}

case class RouteDetail(originGeolocation: Geolocation,
                       destinationAddress: Address,
                       distanceText: String, distanceValue: Long,
                       durationText: String, durationValue: Long,
                       supportedTransports: Option[List[String]])