package repository

import java.util.Date

import dao.embbebedmongo.MongoTest
import dao.util.ShippearDAO
import model.OrderState._
import model._
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.test.Helpers.{await, _}

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

    val repo = new OrderRepoTest

    //Order
    val idUser = "123"
    val idOrder = "idOrder"
    val originGeolocation = Geolocation(132, -123)
    val origin = Address(originGeolocation, "alias", "street", 123, "zipCode", Some("appart"),2, public = true)
    val destinationGeolocation = Geolocation(132, -123)
    val destination = Address(destinationGeolocation, "alias", "aaaaaaa", 1231231, "zipCode", Some("appart"),2, public = true)
    val route = Route(origin, destination)
    val order = Order(idOrder, idUser, "11111", Some("carrierId"),
      NEW, "operationType", route, new Date, new Date, Some("QRCode"))

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
      toState(savedOrder.state) mustEqual NEW

      await(repo.cancelOrder(idOrder))

      val result = await(repo.findOneById(idOrder))
      toState(result.state) mustEqual CANCELLED
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
  val address = Address(geolocation, "alias", "street", 123, "zipCode", Some("appart"),2, public = true)
  val paymentMethod = PaymentMethod("ownerName", "123", new Date, "securityCode", "VISA", None)
  val user = User(idUser, "oneSignalId", "userName", "firstName", "lastName", "36121312", contactInfo, "photoUrl", Seq(address), None, Seq(paymentMethod),
    None, None)

  //Order
  val originGeolocation = Geolocation(132, -123)
  val origin = Address(originGeolocation, "alias", "street", 123, "zipCode", Some("appart"),2, public = true)
  val destinationGeolocation = Geolocation(132, -123)
  val destination = Address(destinationGeolocation, "alias", "aaaaaaa", 1231231, "zipCode", Some("appart"),2, public = true)
  val route = Route(origin, destination)
  val order = Order("idOrder", idUser, "11111", Some("carrierId"),
    "state", "operationType", route, new Date, new Date, Some("QRCode"))



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
        "state", "operationType", route, new Date, new Date, Some("QRCode"))

      await(repo.updateUserOrder(idUser, newOrder))

      val u2 = await(repo.findOneById(idUser))
      u2.orders.get.size mustBe 2

    }


  }
}
