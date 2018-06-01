package repository

import java.util.Date

import dao.embbebedmongo.MongoTest
import dao.util.ShippearDAO
import model.internal.OrderState._
import model.internal.UserType.{APPLICANT}
import model.internal._
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
    val originGeolocation = Geolocation(132, -123)
    val originCity = City(2, "Almagro")
    val origin = Address(originGeolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), originCity, public = true)
    val destinationGeolocation = Geolocation(132, -123)
    val destinationCity = City(1, "Nuñez")
    val destination = Address(destinationGeolocation, Some("alias"), "aaaaaaa", 1231231, "zipCode", Some("appart"), destinationCity, public = true)
    val route = Route(origin, destination)
    val carrierId = "791"
    val qrCode = qrCodeGenerator.generateQrImage(idOrder).stream().toByteArray
    val order = Order(idOrder, idUser, "11111", Some("carrierId"),
      PENDING_PARTICIPANT, "operationType", route, new Date, new Date, Some(new Date), Some(new Date), None)

    "Save a new order" in {
      await(repo.create(order))

      val savedOrder = await(repo.findOneById(idOrder))
      savedOrder._id mustEqual order._id

    }

    "Update an order" in {
      await(repo.create(order))

      val savedOrder = await(repo.findOneById(idOrder))
      val newOrder = savedOrder.copy(applicantId = "newId")
      await(repo.update(newOrder))

      val a = await(repo.all)
      a.size mustBe 1

      val result = await(repo.findOneById(idOrder))
      result.applicantId mustEqual "newId"

    }

    "Cancel an order" in {
      await(repo.create(order))

      val savedOrder = await(repo.findOneById(idOrder))
      toState(savedOrder.state) mustEqual PENDING_PARTICIPANT

      await(repo.cancelOrder(idOrder))

      val result = await(repo.findOneById(idOrder))
      toState(result.state) mustEqual CANCELLED
    }

    "Assign carrier" in {
      await(repo.create(order))
      await(repo.assignCarrier(idOrder, carrierId , qrCode))

      val saveOrderWithCarrier = await(repo.findOneById(idOrder))

      toState(saveOrderWithCarrier.state) mustEqual PENDING_PICKUP
      saveOrderWithCarrier.carrierId mustBe Some(carrierId)
    }

    "Validate QR Code successfully" in {
      await(repo.create(order))

      val result = await(repo.validateQrCode(idOrder,idUser,APPLICANT))
      result equals true
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
  val address = Address(geolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), city, public = true)
  val paymentMethod = PaymentMethod("ownerName", "123", "02/20", "securityCode", "VISA")
  val user = User(idUser, "oneSignalId", "userName", "firstName", "lastName", "36121312",
    contactInfo, "photoUrl", Seq(address), None, Seq(paymentMethod), None, None, None)

  //Order
  val originGeolocation = Geolocation(132, -123)
  val originCity = City(1, "Parque Patricios")
  val origin = Address(originGeolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), originCity, public = true)
  val destinationGeolocation = Geolocation(132, -123)
  val destinationCity = City(5, "Balvanera")
  val destination = Address(destinationGeolocation, Some("alias"), "aaaaaaa", 1231231, "zipCode", Some("appart"), destinationCity, public = true)
  val route = Route(origin, destination)
  val order = Order("idOrder", idUser, "11111", Some("carrierId"),
    "state", "operationType", route, new Date, new Date, Some(new Date), Some(new Date), None)


    "Create a new order into the user" in {

      await(repo.create(user))

      await(repo.updateUserOrder(idUser, order))

      //Comparing order ids
      val updatedUser = await(repo.findOneById(idUser))
      updatedUser.orders.get.head._id mustEqual order._id

    }

    "Update an existing order" in {
      await(repo.create(user))

      await(repo.updateUserOrder(idUser, order))

      //Comparing order ids
      var updatedUser = await(repo.findOneById(idUser))
      var orders = updatedUser.orders.get

      orders.head._id mustEqual order._id
      //Updating the order
      val newOrder = order.copy(carrierId = Some("otherCarrierId"))
      await(repo.updateUserOrder(idUser, newOrder))

      updatedUser = await(repo.findOneById(idUser))
      orders = updatedUser.orders.get
      orders.size mustEqual 1
      orders.head._id mustEqual order._id

    }

    "Add a new order if the user already has a one" in {
      await(repo.create(user))
      await(repo.updateUserOrder(idUser, order))

      val result = await(repo.findOneById(idUser))
      result.orders.get.size mustBe 1

      //Creating another order
      val newOrderId = "11111"
      val newOrder = Order(newOrderId, idUser, "11111", Some("carrierId"),
        "state", "operationType", route, new Date, new Date, Some(new Date), Some(new Date), None)

      await(repo.updateUserOrder(idUser, newOrder))

      val u2 = await(repo.findOneById(idUser))
      u2.orders.get.size mustBe 2

    }


  }
}
