package model.request

import model.internal.Geolocation

case class RouteRequest(userName: String, geolocationOrigin: Geolocation)