package external

import com.google.inject.Inject
import common.serialization.{SnakeCaseJsonProtocol, _}
import model.internal.{Address, Geolocation}
import model.response.apigoogleresponse.ApiMapsResponse
import model.response.price.RouteDetail
import play.api.libs.ws.{WSClient, WSResponse}
import service.PriceService

import scala.concurrent.{ExecutionContext, Future}

class GoogleMapsClient @Inject()(ws: WSClient, priceService: PriceService)(implicit ec: ExecutionContext) extends SnakeCaseJsonProtocol {

  val ApiKey = "AIzaSyDVVJd5t5m0aIBMNIo4q6NK6Dnr5-nWVfM"
  val DistanceMatrixUrl = "https://maps.googleapis.com/maps/api/distancematrix/json"
  val Origins = "origins"
  val Destinations = "destinations"

  def searchDestinationsData(origin: Geolocation, listAddress: Seq[Address]): Seq[Future[List[RouteDetail]]] = {
    val originGeolocation = s"${origin.latitude},${origin.longitude}"

   listAddress.map { address =>

      val destinationGeolocation = s"${address.geolocation.latitude},${address.geolocation.longitude}"

      val apiResponse: Future[WSResponse] =
        ws.url(DistanceMatrixUrl)
          .withQueryStringParameters((Origins, originGeolocation), (Destinations, destinationGeolocation), ("key", ApiKey))
          .get


      apiResponse.map { response =>
        val apiMapsResponse = response.body.parseJsonTo[ApiMapsResponse]

        apiMapsResponse.rows.flatMap {
          row =>
            // distance in meters
            // duration in seconds
            row.elements.map {
              elem => RouteDetail(origin,
                address,
                elem.distance.text,
                elem.distance.value,
                elem.duration.text,
                elem.duration.value,
                None)
            }
        }
      }

    }
  }

}
