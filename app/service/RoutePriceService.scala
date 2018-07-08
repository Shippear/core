package service

import com.google.inject.Inject
import external.GoogleMapsClient
import model.internal.{Address, User}
import model.request.RouteRequest
import model.response.price.RoutePriceResponse

import scala.concurrent.{ExecutionContext, Future}

class RoutePriceService @Inject()(userService: UserService,
                                  priceService: PriceService,
                                  googleApi: GoogleMapsClient)(implicit ec: ExecutionContext) {

  private def searchRouteDetails(routeRequest: RouteRequest, listAddress: Seq[Address]) =
    Future.sequence(googleApi.searchDestinationsData(routeRequest.geolocationOrigin, listAddress))
      .map(_.flatten.toList)

  def priceInformation(routeRequest: RouteRequest): Future[List[RoutePriceResponse]] = {

    val user: Future[User] = userService.findBy(Map("userName" -> routeRequest.userName))
    val userAddress: Future[Seq[Address]] = user.map { u => u.addresses }

    userAddress.flatMap { listAddress =>
      for {
        routes <- searchRouteDetails(routeRequest, listAddress)
        routesPrices <- priceService.calculatePrice(routes, routeRequest.size, routeRequest.weight)
      } yield routesPrices
    }
  }
}