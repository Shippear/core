package service

import com.google.inject.Inject
import model.internal.price.enum.Size.Size
import model.internal.price.enum.Weight.Weight
import model.response.price.{PriceInformation, RouteDetail, RoutePriceResponse}
import repository.price.Scenario._
import repository.price.{DistanceMultiplierRepository, SizeMultiplierRepository, WeightMultiplierRepository}

import scala.concurrent.{ExecutionContext, Future}

class PriceService @Inject()(sizeMultiplierRepository: SizeMultiplierRepository,
                             weightMultiplierRepository: WeightMultiplierRepository,
                             distanceMultiplierRepository: DistanceMultiplierRepository)
                            (implicit ec: ExecutionContext){

  def calculatePrice(routesDetail: List[RouteDetail],
                     size: Size,
                     weight: Weight,
                     scenario: Scenario = NORMAL): Future[List[RoutePriceResponse]] = {

    val prices = routesDetail.map { detail =>

      val distance = detail.distanceValue

      for {
        sizeMult <- sizeMultiplierRepository.multiplier(size)
        weightMult <- weightMultiplierRepository.multiplier(weight)
        scenarioPriceMult <- distanceMultiplierRepository.multiplierByScenario(scenario)
        distanceToKms = distance * 0.001
        totalPrice = sizeMult * weightMult * scenarioPriceMult * distanceToKms
        price = PriceInformation(size, weight, totalPrice)
        routePrice = RoutePriceResponse(detail, price)
      } yield routePrice

    }

    Future.sequence(prices)

  }

}
