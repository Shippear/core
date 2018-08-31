package service

import model.internal.{Address, City, Geolocation, TransportType}
import model.internal.price.enum.{Size, Weight}
import model.response.price.RouteDetail
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers.{await, _}
import repository.price.{DistanceMultiplierRepository, Scenario, SizeMultiplierRepository, WeightMultiplierRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PriceServiceTest extends PlaySpec with MockitoSugar {

  "Price Service" should {

    val destinationCity = City(2, "Almagro")
    val originGeo = Geolocation(1,1)
    val destinationGeo = Geolocation(1,1)
    val destination = Address(destinationGeo, Some("alias"), "street", 123, "zipCode", Some("appart"), destinationCity, public = true, None, None)
    val supportedTransports = Some(TransportType.values.map(_.toString).toList)
    // 5 kms
    val distance = "5"
    val distanceMeters = 5000
    // 7200 seconds = 2hours
    val duration = "2 hours"
    val durationSeconds = 7200
    val routeDetail = RouteDetail(originGeo, destination, distance, distanceMeters, duration, durationSeconds, supportedTransports)
    val listRoutes = List(routeDetail)

    val sizePriceSmall = 2.0
    val sizePriceMedium = 2.5
    val sizePriceBig = 3.0

    val weightPriceLight = 2.0
    val weightPriceMedium = 3.0
    val weightPriceHeavy = 4.0

    val priceNormal = 1.0
    val priceUrgent = 3.0

    val sizeMultMock = mock[SizeMultiplierRepository]
    when(sizeMultMock.multiplier(Size.SMALL)).thenReturn(Future(sizePriceSmall))
    when(sizeMultMock.multiplier(Size.MEDIUM)).thenReturn(Future(sizePriceMedium))
    when(sizeMultMock.multiplier(Size.BIG)).thenReturn(Future(sizePriceBig))

    val weightMultMock = mock[WeightMultiplierRepository]
    when(weightMultMock.multiplier(Weight.LIGHT)).thenReturn(Future(weightPriceLight))
    when(weightMultMock.multiplier(Weight.MEDIUM)).thenReturn(Future(weightPriceMedium))
    when(weightMultMock.multiplier(Weight.HEAVY)).thenReturn(Future(weightPriceHeavy))

    val distanceMultMock = mock[DistanceMultiplierRepository]
    when(distanceMultMock.multiplierByScenario(Scenario.NORMAL)).thenReturn(Future(priceNormal))
    when(distanceMultMock.multiplierByScenario(Scenario.URGENT)).thenReturn(Future(priceUrgent))

    val priceService = new PriceService(sizeMultMock, weightMultMock, distanceMultMock)

    "calculate the price correctly" in {

      // Distance = 5
      // Scenario = 1
      // Small = 2
      // Heavy = 4
      // 5 * 1 * 2 * 4 = 40
      var result = await(priceService.calculatePriceAndTransports(listRoutes, Size.SMALL, Weight.HEAVY)).head
      result.priceInformation.price mustBe 40

      // Distance = 5
      // Scenario = 1
      // Medium = 2.5
      // Medium = 3
      // 5 * 1 * 2.5 * 3 = 37.5
      result = await(priceService.calculatePriceAndTransports(listRoutes, Size.MEDIUM, Weight.MEDIUM)).head
      result.priceInformation.price mustBe 37.5

      //Urgent!
      // Distance = 5
      // Scenario = 3
      // Medium = 2.5
      // Medium = 3
      // 5 * 1 * 2.5 * 3 = 37.5
      result = await(priceService.calculatePriceAndTransports(listRoutes, Size.MEDIUM, Weight.MEDIUM, Scenario.URGENT)).head
      result.priceInformation.price mustBe 112.5


    }

    "Filter the supported transports" in {
      var transports = priceService.supportedTransport(1, Size.SMALL, Weight.LIGHT)
      transports must have size TransportType.values.toList.size
      transports must contain(TransportType.WALKING.toString)
      transports must contain(TransportType.BICYCLE.toString)
      transports must contain(TransportType.MOTORCYCLE.toString)
      transports must contain(TransportType.CAR.toString)

      transports = priceService.supportedTransport(9, Size.SMALL, Weight.LIGHT)
      transports must have size 3
      transports must contain(TransportType.BICYCLE.toString)
      transports must contain(TransportType.MOTORCYCLE.toString)
      transports must contain(TransportType.CAR.toString)

      transports = priceService.supportedTransport(16, Size.SMALL, Weight.LIGHT)
      transports must have size 2
      transports must contain(TransportType.MOTORCYCLE.toString)
      transports must contain(TransportType.CAR.toString)

      transports = priceService.supportedTransport(1, Size.SMALL, Weight.MEDIUM)
      transports must have size TransportType.values.toList.size
      transports must contain(TransportType.WALKING.toString)
      transports must contain(TransportType.BICYCLE.toString)
      transports must contain(TransportType.MOTORCYCLE.toString)
      transports must contain(TransportType.CAR.toString)

      transports = priceService.supportedTransport(1, Size.SMALL, Weight.HEAVY)
      transports must have size 2
      transports must contain(TransportType.MOTORCYCLE.toString)
      transports must contain(TransportType.CAR.toString)

      transports = priceService.supportedTransport(1, Size.MEDIUM, Weight.LIGHT)
      transports must have size TransportType.values.toList.size
      transports must contain(TransportType.WALKING.toString)
      transports must contain(TransportType.BICYCLE.toString)
      transports must contain(TransportType.MOTORCYCLE.toString)
      transports must contain(TransportType.CAR.toString)

      transports = priceService.supportedTransport(1, Size.BIG, Weight.LIGHT)
      transports must have size 1
      transports must contain(TransportType.CAR.toString)




    }

  }

}