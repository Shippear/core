package service

import com.google.inject.Inject
import external.GoogleMapsClient
import model.internal.{Address, User}
import model.mapper.OrderMapper
import model.request.RouteRequest
import model.response.price.UserPriceInformation

import scala.concurrent.{ExecutionContext, Future}

class RoutePriceService @Inject()(userService: UserService,
                                  priceService: PriceService,
                                  googleApi: GoogleMapsClient)(implicit ec: ExecutionContext) {

  private def searchRouteDetails(routeRequest: RouteRequest, listAddress: Seq[Address]) =
    Future.sequence(googleApi.searchDestinationsData(routeRequest.geolocationOrigin, listAddress))
      .map(_.flatten.toList)

  def priceInformation(routeRequest: RouteRequest): Future[UserPriceInformation] = {

    val user: Future[User] = userService.findOneBy(Map("userName" -> routeRequest.userName))
    val userAddress: Future[Seq[Address]] = user.map { u => u.addresses }

    val routesPrices = userAddress.flatMap { listAddress =>
      for {
        routes <- searchRouteDetails(routeRequest, listAddress)
        routesPrices <- priceService.calculatePriceAndTransports(routes, routeRequest.size, routeRequest.weight)
      } yield routesPrices

    }

    for{
      u <- user
      user = OrderMapper.extractUserData(u)
      routePrice <- routesPrices
    } yield UserPriceInformation(user, routePrice)
  }
}