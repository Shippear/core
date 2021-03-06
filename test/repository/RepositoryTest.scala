package repository

import java.util.Date

import common.DateTimeNow._
import dao.util.ShippearDAO
import embbebedmongo.MongoTest
import model.internal.AppType._
import model.internal.OperationType._
import model.internal.OrderState.{PENDING_PARTICIPANT, _}
import model.internal.TransportType._
import model.internal._
import model.internal.price.enum.Size._
import model.internal.price.enum.Weight._
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.test.Helpers.{await, _}
import qrcodegenerator.QrCodeGenerator

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RepositoryTest extends MongoTest {

  "OrderRepository" should {

    val user = mock[User]
    val userRepo = mock[UserRepository]

    when(userRepo.updateUserOrder(any[String], any[Order])).thenReturn(Future(user))
    when(userRepo.findOneById(any[String])).thenReturn(Future(user))

    class OrderRepoTest extends OrderRepository(userRepo){
      override def collectionName: String = "orders"

      override lazy val dao: ShippearDAO[Order] = new ShippearDAO[Order](collectionName, dbContext)
    }

    val qrCodeGenerator = new QrCodeGenerator
    val repo = new OrderRepoTest

    //Order
    val idUser = "123"
    val idOrder = "idOrder"
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
    val qrCode = Some(qrCodeGenerator.generateQrImage(idOrder).stream().toByteArray)
    val applicantData = UserDataOrder(idUser, "name", "last", birthDate, contactInfo, "photo", "notification", Some(0), Some(SENDER))
    val participantData = UserDataOrder("11111", "name", "last", birthDate, contactInfo, "photo", "notification", Some(0), Some(RECEIVER))
    val carrierData = UserDataOrder("carrierId", "name", "last", birthDate, contactInfo, "photo", "notification", Some(0), None)
    val order = Order(idOrder, applicantData, participantData, Some(carrierData), None, 123, "description",
      PENDING_PARTICIPANT, SENDER, SMALL, HEAVY, List(MOTORCYCLE), route, new Date,
      new Date, Some(new Date), None, None, None, None, visa, 0, Some(0), None)

    "Save a new order" in {
      await(repo.create(order))

      val savedOrder = await(repo.findOneById(idOrder))
      savedOrder._id mustEqual order._id

    }

    "Update an order" in {
      await(repo.create(order))

      val savedOrder = await(repo.findOneById(idOrder))
      val newApplicant = applicantData.copy(id = "newId")
      val newOrder = savedOrder.copy(applicant = newApplicant)
      await(repo.update(newOrder))

      val a = await(repo.all)
      a.size mustBe 1

      val result = await(repo.findOneById(idOrder))
      result.applicant.id mustEqual "newId"

    }

    "Cancel an order" in {
      await(repo.create(order))

      val savedOrder = await(repo.findOneById(idOrder))
      toState(savedOrder.state) mustEqual PENDING_PARTICIPANT

      await(repo.cancelOrder(savedOrder))

      val result = await(repo.findOneById(idOrder))
      toState(result.state) mustEqual CANCELLED
    }

    "Assign carrier" in {
      when(user._id).thenReturn(carrierData.id)
      when(user.firstName).thenReturn("name")
      when(user.lastName).thenReturn("last")
      when(user.birthDate).thenReturn(carrierData.birthDate)
      when(user.contactInfo).thenReturn(carrierData.contactInfo)
      when(user.photoUrl).thenReturn("photo")
      when(user.onesignalId).thenReturn("notification")
      when(user.scoring).thenReturn(Some(0f))
      await(repo.create(order))
      await(repo.assignCarrier(order, user , qrCode, Some("urlCode")))

      val saveOrderWithCarrier = await(repo.findOneById(idOrder))

      toState(saveOrderWithCarrier.state) mustEqual PENDING_PICKUP
      saveOrderWithCarrier.carrier mustBe Some(carrierData)
    }
  }


  "UserRepository" should {

  class UserRepoTest extends UserRepository  {
    override def collectionName: String = "users"

    override lazy val dao: ShippearDAO[User] = new ShippearDAO[User](collectionName, dbContext)

  }

    val repo = new UserRepoTest

    //User
    val idUser = "123"
    val geolocation = Geolocation(132, -123)
    val contactInfo = ContactInfo("email@email.com", "011123119")
    val city = City(2, "Almagro")
    val address = Address(geolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), city, public = true, None, None)
    val paymentMethod = PaymentMethod("ownerName", "123", Some("cardCode"), Some("bankCode"), "02/20", "securityCode", Some("VISA"))
    val user = User(idUser, USER, "oneSignalId", "userName", "firstName", "lastName", "36121312", rightNowTime,
      contactInfo, "photoUrl", Seq(address), None, Some(Seq(paymentMethod)), None, None, None)

    //Order
    val originGeolocation = Geolocation(132, -123)
    val originCity = City(1, "Parque Patricios")
    val visa = PaymentMethod("ownerName", "123", Some("cardCode"), Some("bankCode"), "02/20", "securityCode", Some("VISA"))
    val origin = Address(originGeolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), originCity, public = true, None, None)
    val destinationGeolocation = Geolocation(132, -123)
    val destinationCity = City(5, "Balvanera")
    val destination = Address(destinationGeolocation, Some("alias"), "aaaaaaa", 1231231, "zipCode", Some("appart"), destinationCity, public = true, None, None)
    val route = Route(origin, destination, None)
    val birthDate = rightNowTime
    val applicantData = UserDataOrder(idUser, "name", "last", birthDate, contactInfo, "photo", "oneSignal", Some(0), Some(SENDER))
    val participantData = UserDataOrder("11111", "name", "last", birthDate, contactInfo, "photo", "oneSignal", Some(0), Some(RECEIVER))
    val carrierData = UserDataOrder("carrierId", "name", "last", birthDate, contactInfo, "photo", "oneSignal", Some(0), None)
    val order = Order("idOrder", applicantData, participantData, Some(carrierData), None, 123, "description",
      PENDING_PARTICIPANT, SENDER, SMALL, HEAVY, List(MOTORCYCLE), route, new Date,
      new Date, Some(new Date), None, None, None, None, visa, 0, Some(0), None)


    "Create a new order into the user" in {

      await(repo.create(user))

      await(repo.updateUserOrder(applicantData.id, order))

      //Comparing order ids
      val updatedUser = await(repo.findOneById(applicantData.id))
      updatedUser.orders.get.head._id mustEqual order._id

    }

    "Update an existing order" in {
      await(repo.create(user))

      await(repo.updateUserOrder(applicantData.id, order))

      //Comparing order ids
      var updatedUser = await(repo.findOneById(applicantData.id))
      var orders = updatedUser.orders.get

      orders.head._id mustEqual order._id
      //Updating the order
      val newCarrier = carrierData.copy(id = "otherCarrierId")
      val newOrder = order.copy(carrier = Some(newCarrier))
      await(repo.updateUserOrder(applicantData.id, newOrder))

      updatedUser = await(repo.findOneById(applicantData.id))
      orders = updatedUser.orders.get
      orders.size mustEqual 1
      orders.head._id mustEqual order._id

    }

    "Add a new order if the user has already one" in {
      await(repo.create(user))
      await(repo.updateUserOrder(idUser, order))

      val result = await(repo.findOneById(idUser))
      result.orders.get.size mustBe 1

      //Creating another order
      val newOrderId = "11111"
      val visa = PaymentMethod("ownerName", "123", Some("cardCode"), Some("bankCode"), "02/20", "securityCode", Some("VISA"))
      val newOrder = Order(newOrderId, applicantData, participantData, Some(carrierData), None, 123, "description",
        PENDING_PARTICIPANT, SENDER, SMALL, HEAVY, List(MOTORCYCLE), route, new Date,
        new Date, Some(new Date), None, None, None, None, visa, 0, Some(0), None)

      await(repo.updateUserOrder(applicantData.id, newOrder))

      val u2 = await(repo.findOneById(applicantData.id))
      u2.orders.get.size mustBe 2

    }


  }
}
