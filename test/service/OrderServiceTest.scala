package service

import java.util.Date

import com.github.nscala_time.time.Imports.DateTime
import common.DateTimeNow._
import model.internal.OperationType._
import model.internal.OrderState.{ON_TRAVEL, PENDING_PICKUP, DELIVERED}
import model.internal.TransportType._
import model.internal.UserType.{APPLICANT, CARRIER, PARTICIPANT}
import model.internal._
import model.internal.price.enum.Size._
import model.internal.price.enum.Weight._
import model.request.{CancelOrder, OrderCreation}
import onesignal.OneSignalClient
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import model.internal.AppType._
import play.api.test.Helpers.{await, _}
import qrcodegenerator.QrCodeGenerator
import repository.{OrderRepository, UserRepository}
import service.Exception.{NotFoundException, ShippearException}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OrderServiceTest extends PlaySpec with MockitoSugar {

  val carrierId = "carrierId"
  val visa = PaymentMethod("ownerName", "123", Some("cardCode"), Some("bankCode"), "02/20", "securityCode", Some("VISA"))
  val originGeolocation = Geolocation(132, -123)
  val originCity = City(2, "Almagro")
  val origin = Address(originGeolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), originCity, public = true, None, None)
  val destinationGeolocation = Geolocation(132, -123)
  val destinationCity = City(1, "Nuñez")
  val destination = Address(destinationGeolocation, Some("alias"), "aaaaaaa", 1231231, "zipCode", Some("appart"), destinationCity, public = true, None, None)
  val route = Route(origin, destination, None)

  val birthDate = rightNowTime
  val contactInfo = ContactInfo("email@email.com", "011123119")

  val applicantData = UserDataOrder("12345", "name", "last", birthDate, contactInfo, "photo", "onesignal", Some(0), Some(SENDER))
  val participantData = UserDataOrder("123", "name", "last", birthDate, contactInfo, "photo", "onesignal", Some(0), Some(RECEIVER))
  val carrierData = UserDataOrder("carrierId", "name", "last", birthDate, contactInfo, "photo", "onesignal", Some(0), None)

  val order_1 = Order("1", applicantData, participantData, Some(carrierData), None, 123, "description",
    ON_TRAVEL, SENDER, SMALL, HEAVY, List(MOTORCYCLE), route, new Date,
    new Date, Some(new Date), None, None, None, visa, 0, Some(0), None)
  val order_2 = Order("2", applicantData, participantData, Some(carrierData), None, 123, "description",
    ON_TRAVEL, SENDER, SMALL, HEAVY, List(MOTORCYCLE), route, new Date,
    new Date, Some(new Date), None, None, None, visa, 0, Some(0), None)
  val order_3 = Order("3", applicantData, participantData, Some(carrierData), None, 123, "description",
    ON_TRAVEL, SENDER, SMALL, HEAVY, List(MOTORCYCLE), route, new Date,
    new Date, Some(new Date), None, None, None, visa, 0, Some(0), None)

  val otherCarrier = UserDataOrder("other", "name", "last", birthDate, contactInfo, "photo", "onesignal", Some(0), None)
  val order_bla = Order("4", carrierData, participantData, Some(otherCarrier), None, 123, "description",
    ON_TRAVEL, SENDER, SMALL, HEAVY, List(MOTORCYCLE), route, new Date,
    new Date, Some(new Date), None, None, None, visa, 0, Some(0), None)

  val orderWithoutCarrier: Order = order_1.copy(carrier = None)


  //User
  val geolocation = Geolocation(132, -123)
  val city = City(2, "Almagro")
  val address = Address(geolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), city, public = true, None, None)
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
      val afterTomorrow = today.plusDays(2)

      //Duration in seconds so...
      //2 hours = 2 * 60 * 60 = 7200
      val duration = 7200

      val orderCreation = OrderCreation(None, "a", "b", "description",
        SENDER, SMALL, HEAVY, List(MOTORCYCLE),
        route, today, tomorrow, None, visa, 0, duration)

      orderService.validateAvailableTimes(orderCreation)

      intercept[ShippearException]{
        val orderCreationInvalidBegin = orderCreation.copy(availableFrom = yesterday)
        orderService.validateAvailableTimes(orderCreationInvalidBegin)
      }

      intercept[ShippearException]{
        val orderCreationInvalidRange = orderCreation.copy(availableFrom = afterTomorrow, availableTo = tomorrow)
        orderService.validateAvailableTimes(orderCreationInvalidRange)
      }

      //When the operation is RECEIVE
      val orderTypeReceive = OrderCreation(None, "a", "b", "desc", RECEIVER, SMALL,
        HEAVY, List(WALKING), route, today.plusSeconds(duration), afterTomorrow, None, visa, 0, duration)
      orderService.validateAvailableTimes(orderTypeReceive)

      intercept[ShippearException]{
        val orderReceive = OrderCreation(None, "a", "b", "desc", RECEIVER, SMALL,
          HEAVY, List(WALKING), route, today, tomorrow, None, visa, 0, duration)
        orderService.validateAvailableTimes(orderReceive)
      }


    }

    "Validate correctly a carrier with 3 orders ON_TRAVEL" in {
      // 2 Orders
      val orders = Some(List(order_1, order_2))
      val user = User(carrierId, AppType.CARRIER, "oneSignalId", "usxerName", "firstName", "lastName", "36121312", rightNowTime,
        contactInfo, "photoUrl", Seq(address), orders, Some(Seq(paymentMethod)), None, None, None)

      orderService.validateCarrier(user)

      val orders4 = Some(List(order_1, order_2, order_bla))
      val user_2 = User(carrierId, AppType.CARRIER, "oneSignalId", "userName", "firstName", "lastName", "36121312", rightNowTime,
        contactInfo, "photoUrl", Seq(address), orders4, Some(Seq(paymentMethod)), None, None, None)

      orderService.validateCarrier(user_2)
    }

    "Throw ShippearException when the carrier has 3 orders ON_TRAVEL" in {
      // 3 Orders
      val orders = Some(List(order_1, order_2, order_3))
      val user = User(carrierId, AppType.CARRIER, "oneSignalId", "userName", "firstName", "lastName", "36121312", rightNowTime,
        contactInfo, "photoUrl", Seq(address), orders, Some(Seq(paymentMethod)), None, None, None)

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

    "Validate when trying to cancel an order" in {
      when(repo.findOneById(order_1._id)).thenReturn(Future(order_1))

      //Order is in ON_TRAVEL
      intercept[ShippearException]{
        await(orderService.cancelOrder(CancelOrder(order_1._id, APPLICANT)))
      }
    }

    "Rating the Carrier correctly" in {
      val ratedOrder_1 = order_1.copy(state = DELIVERED, ratedCarrier = Some(true), ratedValue = Some(5))
      val ratedOrder_2 = order_2.copy(state = DELIVERED, ratedCarrier = Some(true), ratedValue = Some(4))
      val ratedOrder_3 = order_3.copy(state = DELIVERED, ratedCarrier = Some(true), ratedValue = Some(2))

      val unRatedOrder_1 = order_1.copy(state = DELIVERED, ratedCarrier = Some(false))

      val orders: Option[Seq[Order]] = Some(Seq(ratedOrder_1, ratedOrder_2, ratedOrder_3, order_1, unRatedOrder_1))


      val carrier = User(carrierId, AppType.CARRIER, "oneSignalId", "userName", "firstName", "lastName", "36121312", rightNowTime,
        contactInfo, "photoUrl", Seq(address), orders, Some(Seq(paymentMethod)), None, None, None)


      val result = orderService.updateCarrierRating(carrier, 4)
      result.scoring.get mustBe 3.75



    }
  }
}
