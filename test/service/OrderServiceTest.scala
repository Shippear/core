package service

import java.util.Date

import com.github.nscala_time.time.Imports.DateTime
import common.DateTimeNow
import model.internal.OrderState.{ON_TRAVEL, PENDING_PICKUP}
import model.internal.UserType.{APPLICANT, CARRIER, PARTICIPANT}
import model.internal._
import model.internal.price.enum.Size._
import model.internal.price.enum.Weight._
import model.internal.OperationType._
import model.internal.TransportType._
import model.request.OrderCreation
import onesignal.OneSignalClient
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import qrcodegenerator.QrCodeGenerator
import repository.{OrderRepository, UserRepository}
import service.Exception.{NotFoundException, ShippearException}

import scala.concurrent.ExecutionContext.Implicits.global

class OrderServiceTest extends PlaySpec with MockitoSugar {

  val carrierId = "carrierId"
  val visa = PaymentMethod("ownerName", "123", Some("cardCode"), Some("bankCode"), "02/20", "securityCode", Some("VISA"))
  val originGeolocation = Geolocation(132, -123)
  val originCity = City(2, "Almagro")
  val origin = Address(originGeolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), originCity, public = true)
  val destinationGeolocation = Geolocation(132, -123)
  val destinationCity = City(1, "Nuñez")
  val destination = Address(destinationGeolocation, Some("alias"), "aaaaaaa", 1231231, "zipCode", Some("appart"), destinationCity, public = true)
  val route = Route(origin, destination)

  val birthDate = DateTimeNow.now.toDate
  val contactInfo = ContactInfo("email@email.com", "011123119")

  val applicantData = UserDataOrder("12345", "name", "last", birthDate, contactInfo, "photo", "onesignal", Some(0), Some(SENDER))
  val participantData = UserDataOrder("123", "name", "last", birthDate, contactInfo, "photo", "onesignal", Some(0), Some(RECEIVER))
  val carrierData = UserDataOrder("carrierId", "name", "last", birthDate, contactInfo, "photo", "onesignal", Some(0), None)

  val order_1 = Order("1", applicantData, participantData, Some(carrierData), 123, "description",
    ON_TRAVEL, SENDER, SMALL, HEAVY, List(MOTORCYCLE), route, new Date,
    new Date, Some(new Date), None, None, visa, 0, Some(0), None)
  val order_2 = Order("2", applicantData, participantData, Some(carrierData), 123, "description",
    ON_TRAVEL, SENDER, SMALL, HEAVY, List(MOTORCYCLE), route, new Date,
    new Date, Some(new Date), None, None, visa, 0, Some(0), None)
  val order_3 = Order("3", applicantData, participantData, Some(carrierData), 123, "description",
    ON_TRAVEL, SENDER, SMALL, HEAVY, List(MOTORCYCLE), route, new Date,
    new Date, Some(new Date), None, None, visa, 0, Some(0), None)

  val otherCarrier = UserDataOrder("other", "name", "last", birthDate, contactInfo, "photo", "onesignal", Some(0), None)
  val order_bla = Order("4", carrierData, participantData, Some(otherCarrier), 123, "description",
    ON_TRAVEL, "operationType", SMALL, HEAVY, List(MOTORCYCLE), route, new Date,
    new Date, Some(new Date), None, None, visa, 0, Some(0), None)

  val orderWithoutCarrier = order_1.copy(carrier = None)


  //User
  val geolocation = Geolocation(132, -123)
  val city = City(2, "Almagro")
  val address = Address(geolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), city, public = true)
  val paymentMethod = PaymentMethod("ownerName", "123", Some("cardCode"), Some("bankCode"), "02/20", "securityCode", Some("VISA"))


  "Order Service" should {

    val repo = mock[OrderRepository]
    val mailClient = mock[OneSignalClient]
    val qrGenerator = mock[QrCodeGenerator]
    val userRepo = mock[UserRepository]

    val orderService = new OrderService(repo, mailClient, qrGenerator, userRepo)

    "Validate the order data" in {
      val today = DateTime.now().plusSeconds(30)
      val yesterday = today.minusDays(1)
      val tomorrow = today.plusDays(1)
      val afterTomorow = today.plusDays(2)

      val orderCreation = OrderCreation(None, "a", "b", "description",
        SENDER, SMALL, HEAVY, List(MOTORCYCLE),
        route, today.toDate, tomorrow.toDate, None, None, visa, 0)

      orderService.validateOrder(orderCreation)

      val orderCreationInvalidBegin = orderCreation.copy(availableFrom = yesterday.toDate)
      intercept[ShippearException]{
        orderService.validateOrder(orderCreationInvalidBegin)
      }

      val orderCreationInvalidRange = orderCreation.copy(availableFrom = afterTomorow.toDate, availableTo = tomorrow.toDate)
      intercept[ShippearException]{
        orderService.validateOrder(orderCreationInvalidRange)
      }
    }

    "Validate correctly a carrier with 3 orders ON_TRAVEL" in {
      // 2 Orders
      val orders = Some(List(order_1, order_2))
      val user = User(carrierId, "oneSignalId", "usxerName", "firstName", "lastName", "36121312", DateTime.now().toDate,
        contactInfo, "photoUrl", Seq(address), orders, Seq(paymentMethod), None, None, None)

      orderService.validateCarrier(user)

      val orders4 = Some(List(order_1, order_2, order_bla))
      val user_2 = User(carrierId, "oneSignalId", "userName", "firstName", "lastName", "36121312", DateTime.now().toDate,
        contactInfo, "photoUrl", Seq(address), orders4, Seq(paymentMethod), None, None, None)

      orderService.validateCarrier(user_2)
    }

    "Throw ShippearException when the carrier has 3 orders ON_TRAVEL" in {
      // 3 Orders
      val orders = Some(List(order_1, order_2, order_3))
      val user = User(carrierId, "oneSignalId", "userName", "firstName", "lastName", "36121312", DateTime.now().toDate,
        contactInfo, "photoUrl", Seq(address), orders, Seq(paymentMethod), None, None, None)

      intercept[ShippearException] {
        orderService.validateCarrier(user)
      }
    }

    "Validate QR Code successfully" in {
      val orderToValidateApplicant = OrderToValidate(order_1._id, applicantData.id, APPLICANT)
      orderService.verifyQR(orderToValidateApplicant, order_1) mustBe true

      val orderToValidateParticipant = OrderToValidate(order_1._id, participantData.id, PARTICIPANT)
      orderService.verifyQR(orderToValidateParticipant, order_1) mustBe true

      val orderPendingPickup = order_1.copy(state = PENDING_PICKUP)
      val orderToValidateCarrier = OrderToValidate(order_1._id, carrierId, CARRIER)
      orderService.verifyQR(orderToValidateCarrier, orderPendingPickup) mustBe true
    }

    "Wrong QR Code" in {
      val orderToValidate = OrderToValidate(order_1._id, applicantData.id, CARRIER)
      orderService.verifyQR(orderToValidate, order_1) mustBe false

      val orderInOtherStatus = OrderToValidate(order_1._id, carrierId, CARRIER)
      orderService.verifyQR(orderInOtherStatus, order_1) mustBe false

    }

    "Throw NotFoundException when the carrier doesn't exists in the order" in {

      val orderToValidate = OrderToValidate(orderWithoutCarrier._id, applicantData.id, CARRIER)

      intercept[NotFoundException] {
        orderService.verifyQR(orderToValidate, orderWithoutCarrier) mustBe true
      }
    }
  }
}
