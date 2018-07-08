package service

import com.google.inject.Inject
import common.ConfigReader
import model.internal.price.enum.Size.Size
import model.internal.price.enum.Weight.Weight
import model.response.price.{PriceInformation, RouteDetail, RoutePriceResponse}
import repository.price.{DistanceMultiplierRepository, Scenario, SizeMultiplierRepository, WeightMultiplierRepository}

import scala.concurrent.{ExecutionContext, Future}

class PriceService @Inject()(sizeMultiplierRepository: SizeMultiplierRepository,
                             weightMultiplierRepository: WeightMultiplierRepository,
                             distanceMultiplierRepository: DistanceMultiplierRepository)
                            (implicit ec: ExecutionContext){

  def calculatePrice(routesDetail: List[RouteDetail],
                     size: Size,
                     weight: Weight): Future[List[RoutePriceResponse]] = {

    val prices = routesDetail.map { detail =>

      val distance = detail.distance.toDouble

      for {
        priceMult <- sizeMultiplierRepository.multiplier(size)
        weightMult <- weightMultiplierRepository.multiplier(weight)
        distancePriceMult <- distanceMultiplierRepository.multiplierByScenario(Scenario.NORMAL)
        totalPrice = priceMult * weightMult * distancePriceMult * distance
        price = PriceInformation(size, weight, totalPrice)
        routePrice = RoutePriceResponse(detail, price)
      } yield routePrice

    }

    Future.sequence(prices)

  }

}
