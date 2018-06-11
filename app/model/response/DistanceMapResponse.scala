package model.response
import model.internal.Geolocation

case class DistanceMapResponse(originAddresses:Geolocation, destinationAddreses:Geolocation, distance: String, duration: String)

case class ApiMapsResponse(rows: List[Element], status: String)

case class Element(elements: List[LocationData])

case class LocationData(distance: TextValue, duration: TextValue)

case class TextValue(text: String, value: Long)