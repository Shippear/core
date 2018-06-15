package model.response
import model.internal.Geolocation

case class DistanceMapResponse(originAddresses:Geolocation, destinationAddresses:Geolocation, distance: String, duration: String)