package service

import java.util.Date

import model.internal._
import model.internal.OrderState.ON_TRAVEL
import onesignal.OneSignalClient
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import qrcodegenerator.QrCodeGenerator
import repository.{OrderRepository, UserRepository}
import service.Exception.ShippearException
import scala.concurrent.ExecutionContext.Implicits.global

class OrderServiceTest extends PlaySpec with MockitoSugar {

  val carrierId = "carrierId"

  val originGeolocation = Geolocation(132, -123)
  val originCity = City(2, "Almagro")
  val origin = Address(originGeolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), originCity , public = true)
  val destinationGeolocation = Geolocation(132, -123)
  val destinationCity = City(1, "Nuñez")
  val destination = Address(destinationGeolocation, Some("alias"), "aaaaaaa", 1231231, "zipCode", Some("appart"), destinationCity, public = true)
  val route = Route(origin, destination)

  val order_1 = Order("1", "12345", "123", Some(carrierId), "description",
    ON_TRAVEL, "operationType", route, new Date, new Date, Some(new Date), Some(new Date), None)
  val order_2 = Order("2", "12345", "123", Some(carrierId), "description",
    ON_TRAVEL, "operationType", route, new Date, new Date, Some(new Date), Some(new Date), None)
  val order_3 = Order("3", "12345", "123", Some(carrierId), "description",
    ON_TRAVEL, "operationType", route, new Date, new Date, Some(new Date), Some(new Date), None)

  val order_bla = Order("4", carrierId, "123", Some("other"), "description",
    ON_TRAVEL, "operationType", route, new Date, new Date, Some(new Date), Some(new Date), None)


  //User
  val geolocation = Geolocation(132, -123)
  val contactInfo = ContactInfo("email@email.com", "011123119")
  val city = City(2, "Almagro")
  val address = Address(geolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), city, public = true)
  val paymentMethod = PaymentMethod("ownerName", "123", "cardCode", "bankCode", "02/20", "securityCode", "VISA")


  "Order Service" should {

    val repo = mock[OrderRepository]
    val mailClient = mock[OneSignalClient]
    val qrGenerator = mock[QrCodeGenerator]
    val userRepo = mock[UserRepository]

    val orderService = new OrderService(repo, mailClient, qrGenerator, userRepo)

    "Validate correctly a carrier with 3 orders ON_TRAVEL" in {
      // 2 Orders
      val orders = Some(List(order_1, order_2))
      val user = User(carrierId, "oneSignalId", "usxerName", "firstName", "lastName", "36121312",
        contactInfo, "photoUrl", Seq(address), orders , Seq(paymentMethod), None, None, None)

      orderService.validateCarrier(user)

      val orders4 = Some(List(order_1, order_2, order_bla))
      val user_2 = User(carrierId, "oneSignalId", "userName", "firstName", "lastName", "36121312",
        contactInfo, "photoUrl", Seq(address), orders4 , Seq(paymentMethod), None, None, None)

      orderService.validateCarrier(user_2)
    }

    "Throw ShippearException when the carrier has 3 orders ON_TRAVEL" in {
      // 3 Orders
      val orders = Some(List(order_1, order_2, order_3))
      val user = User(carrierId, "oneSignalId", "userName", "firstName", "lastName", "36121312",
        contactInfo, "photoUrl", Seq(address), orders, Seq(paymentMethod), None, None, None)

      intercept[ShippearException]{
        orderService.validateCarrier(user)
      }
    }
  }

}
