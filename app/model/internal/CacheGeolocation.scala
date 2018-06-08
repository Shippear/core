package model.internal

import com.fasterxml.jackson.annotation.JsonProperty

case class CacheGeolocation(@JsonProperty("_id") _id: String, geolocation: Geolocation)