package model.response.price

import model.internal.Geolocation

case class RouteDetail(originAddresses: Geolocation,
                       destinationAddresses: Geolocation,
                       distance: String, duration: String)