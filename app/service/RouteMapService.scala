package service

import com.google.inject.Inject
import external.GoogleMapsClient
import model.internal.{Address, User}
import model.request.RouteRequest
import model.response.DistanceMapResponse

import scala.concurrent.{ExecutionContext, Future}

class RouteMapService @Inject()(service: UserService, client: GoogleMapsClient)(implicit ec: ExecutionContext){

  def addressInformation(routeRequest: RouteRequest) = {

    val user: Future[User] = service.repository.findBy(Map("userName" -> routeRequest.userName))
    val userAddress: Future[Seq[Address]] = user.map { u => u.addresses }

    userAddress.flatMap { listAddress =>

      val listData: Seq[Future[List[DistanceMapResponse]]] = client.searchDestinationsData(routeRequest.geolocationOrigin, listAddress)

      Future.sequence(listData).map(_.flatten.toList)


    }
  }
}
