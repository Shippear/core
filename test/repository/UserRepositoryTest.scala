package repository

import java.util.Date

import dao.embbebedmongo.MongoTest
import dao.util.ShippearDAO
import model._
import play.api.test.Helpers.{await, _}
import scala.concurrent.ExecutionContext.Implicits.global

class UserRepositoryTest extends MongoTest {

  class UserRepoTest extends UserRepository  {
    override def collectionName: String = "test"

    override lazy val dao: ShippearDAO[User] = new ShippearDAO[User](collectionName, dbContext)

  }

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


  "UserRepository" should {
    "Create a new order into the user" in {
      val repo = new UserRepoTest
      await(repo.create(user))

      await(repo.updateUserOrder(idUser, order))

      //Comparing order ids
      val updatedUser = await(repo.findOneById(idUser))
      updatedUser.orders.get.head._id mustEqual order._id

    }

    "Update an existing order" in {
      val repo = new UserRepoTest
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
      val repo = new UserRepoTest
      await(repo.create(user))
      await(repo.updateUserOrder(idUser, order))

      val u = await(repo.findOneById(idUser))
      u.orders.get.size mustBe 1

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
