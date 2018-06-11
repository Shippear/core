package external

import com.google.inject.Inject
import common.serialization.{SnakeCaseJsonProtocol, _}
import model.internal.{Address, Geolocation}
import model.response.{ApiMapsResponse, DistanceMapResponse}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class GoogleMapsClient @Inject()(ws: WSClient)(implicit ec: ExecutionContext) extends SnakeCaseJsonProtocol {

  val ApiKey = "AIzaSyDVVJd5t5m0aIBMNIo4q6NK6Dnr5-nWVfM"
  val DistanceMatrixUrl = "https://maps.googleapis.com/maps/api/distancematrix/json"

  def searchDestinationsData(origin: Geolocation, listAddress: Seq[Address]) = {
    val originGeolocation = s"${origin.latitude},${origin.longitude}"

   listAddress.map { address =>

      val destinationGeolocation = s"${address.geolocation.latitude},${address.geolocation.longitude}"

      val apiResponse: Future[WSResponse] =
        ws.url(DistanceMatrixUrl)
          .withQueryStringParameters(("origins", originGeolocation), ("destinations", destinationGeolocation), ("key", ApiKey))
          .get


      apiResponse.map { response =>
        val apiMapsResponse = response.body.parseJsonTo[ApiMapsResponse]

        apiMapsResponse.rows.flatMap {
          row =>
            row.elements.map {
              elem => DistanceMapResponse(origin, address.geolocation, elem.distance.text, elem.duration.text)
            }
        }
      }

    }
  }

}
