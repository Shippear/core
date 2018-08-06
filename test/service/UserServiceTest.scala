package service

import java.util.Date

import com.github.nscala_time.time.Imports.DateTime
import model.internal._
import model.internal.OrderState._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import repository.UserRepository
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import model.internal.price.enum.{Size, Weight}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.test.Helpers.{await, _}
import model.internal._


class UserServiceTest extends PlaySpec with MockitoSugar {

  val originGeolocation = Geolocation(132, -123)
  val originCity = City(2, "Almagro")
  val origin = Address(originGeolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), originCity , public = true)
  val destinationGeolocation = Geolocation(132, -123)
  val destinationCity = City(1, "Nuñez")
  val destination = Address(destinationGeolocation, Some("alias"), "aaaaaaa", 1231231, "zipCode", Some("appart"), destinationCity, public = true)
  val route = Route(origin, destination)

  val applicantData = UserDataOrder("12345", "name", "last", "photo", "onesignal", Some(0))
  val participantData = UserDataOrder("123", "name", "last", "photo", "onesignal", Some(0))
  val carrierData = UserDataOrder("carrierId", "name", "last", "photo", "onesignal", Some(0))


  "OrdersByState" should {

    val toBeConfirmed_1 = Order("1", applicantData, participantData, Some(carrierData), 123, "description",
      PENDING_CARRIER, "operationType", Size.SMALL, Weight.HEAVY, List(TransportType.MOTORCYCLE), route, new Date, new Date, Some(new Date), None, None, None, None)
    val toBeConfirmed_2 = Order("2", applicantData, participantData, Some(carrierData), 123, "description",
      PENDING_CARRIER, "operationType", Size.SMALL, Weight.HEAVY, List(TransportType.MOTORCYCLE), route, new Date, new Date, Some(new Date), None, None, None, None)

    val inProgress_1 = Order("3", applicantData, participantData, Some(carrierData), 123, "description",
      PENDING_PICKUP, "operationType", Size.SMALL, Weight.HEAVY, List(TransportType.MOTORCYCLE), route, new Date, new Date, Some(new Date), None, None, None, None)
    val inProgress_2 = Order("4", applicantData, participantData, Some(carrierData), 123, "description",
      PENDING_PICKUP, "operationType", Size.SMALL, Weight.HEAVY, List(TransportType.MOTORCYCLE), route, new Date, new Date, Some(new Date), None, None, None, None)
    val inProgress_3 = Order("5", applicantData, participantData, Some(carrierData), 123, "description",
      ON_TRAVEL, "operationType", Size.SMALL, Weight.HEAVY, List(TransportType.MOTORCYCLE), route, new Date, new Date, Some(new Date), None, None, None, None)

    val finalized_1 = Order("6", applicantData, participantData, Some(carrierData), 123, "description",
      DELIVERED, "operationType", Size.SMALL, Weight.HEAVY, List(TransportType.MOTORCYCLE), route, new Date, new Date, Some(new Date), None, None, None, None)
    val finalized_2 = Order("7", applicantData, participantData, Some(carrierData), 123, "description",
      CANCELLED, "operationType", Size.SMALL, Weight.HEAVY, List(TransportType.MOTORCYCLE), route, new Date, new Date, Some(new Date), None, None, None, None)


    val orders = Some(Seq(toBeConfirmed_1, toBeConfirmed_2, inProgress_1, inProgress_2, inProgress_3, finalized_1, finalized_2))

    //User
    val idUser = "123"
    val geolocation = Geolocation(132, -123)
    val contactInfo = ContactInfo("email@email.com", "011123119")
    val city = City(2, "Almagro")
    val address = Address(geolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), city, public = true)
    val paymentMethod = PaymentMethod("ownerName", "123", Some("cardCode"), Some("bankCode"), "02/20", "securityCode", Some("VISA"))
    val user = User(idUser, "oneSignalId", "userName", "firstName", "lastName", "36121312", DateTime.now().toDate,
      contactInfo, "photoUrl", Seq(address), orders, Seq(paymentMethod), None, None, None)

    val repo = mock[UserRepository]
    when(repo.findOneById(any[String])).thenReturn(Future(user))

    "classify by order state" in {
      val service = new UserService(repo)

      val userResponse = await(service.ordersByState("bla"))

      val toBeConfirmed = userResponse.ordersToBeConfirmed.get
      toBeConfirmed.size mustBe 2
      toBeConfirmed must contain(toBeConfirmed_1)
      toBeConfirmed must contain(toBeConfirmed_2)

      val inProgress = userResponse.ordersInProgress.get
      inProgress.size mustBe 3
      inProgress must contain(inProgress_1)
      inProgress must contain(inProgress_2)
      inProgress must contain(inProgress_3)

      val finalized = userResponse.ordersFinalized.get
      finalized.size mustBe 2
      finalized must contain(finalized_1)
      finalized must contain(finalized_2)

    }

  }



}
