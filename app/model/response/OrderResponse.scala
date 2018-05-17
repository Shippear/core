package model.response

import model.Route
import org.joda.time.DateTime

case class OrderResponse(_id: String, state: String, operationType: String, route: Route,
                         availableFrom: DateTime, availableTo: DateTime,
                         arrivalFrom: DateTime, arrivalTo: DateTime)
