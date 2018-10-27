package service

import com.google.inject.Inject
import model.internal.TransportType
import model.internal.TransportType.TransportType
import model.internal.price.enum.Size.Size
import model.internal.price.enum.Weight.Weight
import model.internal.price.enum.{Size, Weight}
import model.response.price.{PriceInformation, RouteDetail, RoutePriceResponse}
import repository.price.Scenario._
import repository.price.{DistanceMultiplierRepository, SizeMultiplierRepository, WeightMultiplierRepository}

import scala.concurrent.{ExecutionContext, Future}

class PriceService @Inject()(sizeMultiplierRepository: SizeMultiplierRepository,
                             weightMultiplierRepository: WeightMultiplierRepository,
                             distanceMultiplierRepository: DistanceMultiplierRepository)
                            (implicit ec: ExecutionContext){

  def calculatePriceAndTransports(routesDetail: List[RouteDetail],
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
        transports = supportedTransport(distanceToKms, size, weight)
        totalPrice = sizeMult * weightMult * scenarioPriceMult * distanceToKms + basePrice(transports)
        price = PriceInformation(size, weight, totalPrice)
        routePrice = RoutePriceResponse(detail.copy(supportedTransports = Some(transports.map(_.toString))), price)
      } yield routePrice

    }

    Future.sequence(prices)

  }

  private def basePrice(transports: List[TransportType]): Double = {
    if(transports.contains(TransportType.WALKING) || transports.contains(TransportType.BICYCLE))
      20
    else
      50
  }


  def supportedTransport(distance: Double, size: Size, weight: Weight): List[TransportType] = {
    var transports = TransportType.values.toBuffer[TransportType]

    // Distance in Kms
    if(distance > 15)
      transports = transports.filter(t => t.equals(TransportType.CAR) || t.equals(TransportType.MOTORCYCLE))
    else if(distance > 8)
      transports = transports.filterNot(_.equals(TransportType.WALKING))


    // Weight
    if(weight.equals(Weight.HEAVY))
      transports = transports.filter(t =>
        t.eq(TransportType.CAR) || t.equals(TransportType.MOTORCYCLE))

    // Size
    if(size.equals(Size.BIG))
      transports = transports.filter(_.equals(TransportType.CAR))

    transports.toList

  }

}
