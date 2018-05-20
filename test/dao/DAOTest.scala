package dao

import java.util.Date

import dao.embbebedmongo.MongoTest
import dao.util.ShippearDAO
import model._
import model.internal.{Address, Geolocation, Order, Route}
import org.joda.time.DateTime
import org.mongodb.scala.model.Filters
import play.api.test.Helpers.{await, _}
import repository.ShippearRepository
import service.Exception.NotFoundException

import scala.concurrent.ExecutionContext.Implicits.global


class DAOTest extends MongoTest with ShippearRepository[Order] {

  override def collectionName: String = "test"

  lazy val dao: ShippearDAO[Order] = new ShippearDAO[Order](collectionName, dbContext)

  val originGeolocation = Geolocation(132, -123)
  val origin = Address(originGeolocation, Some("alias"), "street", 123, "zipCode", Some("appart"),2, public = true)
  val destinationGeolocation = Geolocation(132, -123)
  val destination = Address(destinationGeolocation, Some("alias"), "aaaaaaa", 1231231, "zipCode", Some("appart"),2, public = true)
  val route = Route(origin, destination)

  lazy val participantId = "11111"
  val order = Order("123", "12345", participantId, Some("carrierId"),
    "state", "operationType", route, new Date, new Date, Some(new Date), Some(new Date), Some("QRCode"))


  "OrderDAO" should{
    "Save an object" in {
      await(dao.insertOne(order))
      await(dao.all).size mustBe 1
    }

    "Update an object" in {
      order.participantId mustBe participantId
      await(dao.all).size mustBe 0
      await(dao.insertOne(order))
      await(dao.all).size mustBe 1
      val orderFound = await(dao.findOneById(order._id) )

      val newOrder = orderFound.copy(participantId = "bla")
      await(dao.replaceOne(newOrder))
      val newOrderFound = await(dao.findOneById(order._id))
      newOrderFound.participantId mustBe "bla"
    }

    "Throw a NotFoundException when and object doesn't exists in the DB" in {
      intercept[NotFoundException]{
        await(dao.findOneById("bla"))
      }
    }

    "Find by a filter" in {
      await(dao.insertOne(order))

      //Filters
      //eq
      await(dao.findByFilters(Filters.eq("participantId", participantId))).size mustBe 1
      await(dao.findByFilters(Filters.eq("participantId", "AAAA"))).size mustBe 0

      //greater than & lower than
      val availableFrom = new DateTime().minusDays(1).toDate
      await(dao.findByFilters(Filters.gt("availableFrom", availableFrom))).size mustBe 1
      await(dao.findByFilters(Filters.lt("availableFrom", availableFrom))).size mustBe 0

      //using and
      val filterAnd = Filters.and(Filters.eq("participantId", participantId), Filters.lt("availableFrom", availableFrom))
      await(dao.findByFilters(filterAnd)).size mustBe 0

      //using or
      val filterOr = Filters.or(Filters.eq("participantId", participantId), Filters.lt("availableFrom", availableFrom))
      await(dao.findByFilters(filterOr)).size mustBe 1
    }


  }
}
