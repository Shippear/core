package repository

import java.util.Date

import dao.embbebedmongo.MongoTest
import dao.util.ShippearDAO
import model._
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers.{await, _}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import model.OrderState._

class OrderRepositoryTest extends MongoTest with MockitoSugar {

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

  "OrderRepository" should {
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


}
