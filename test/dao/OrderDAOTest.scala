package dao

import java.util.Date

import com.fasterxml.jackson.annotation.JsonProperty
import dao.embbebedmongo.{MongoTest, ToDocument}
import model._
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global


class OrderDAOTest extends MongoTest with ToDocument[Order] {

  val originGeolocation = Geolocation(132, -123)
  val origin = Address(originGeolocation, "alias", "street", 123, "zipCode", Some("appart"),2, Some(true))
  val destinationGeolocation = Geolocation(132, -123)
  val destination = Address(destinationGeolocation, "alias", "aaaaaaa", 1231231, "zipCode", Some("appart"),2, Some(true))
  val route = Route(origin, destination)

  val order = Order("123", "12345", "participantId", Some("carrierId"),
    "state", "operationType", route, new Date, new Date, Some("QRCode"))


  "OrderDAO" should {
    "save" in {
      val dao = new OrderDAO(dbContext)

      await(dao.insertOne(order))
      await(dao.collection.count().toFuture()) mustBe 1

      val otherOrder = order.copy(_id = "otroId")
      await(dao.insertOne(otherOrder))
      await(dao.collection.count().toFuture()) mustBe 2


    }

    "find" in {
      val dao = new OrderDAO(dbContext)
      await(dao.insertOne(order))

      val bson: BsonDocument = Filters.equal("applicantId", order.applicantId).toBsonDocument(Filters.getClass, dbContext.dbContext.codecRegistry)
      val filter = Document(bson)

      await(dao.findOne(filter)).size mustBe 1
      await(dao.collection.find(Filters.equal("carrierId", "blablabla")).toFuture()).size mustBe 0

      await(dao.collection.find(Filters.gt("_id", 123)).toFuture()).size mustBe 0

      await(dao.collection.find(Filters.equal("route.origin.alias", order.route.origin.alias)).toFuture()).size mustBe 1

    }

    "update" in {
      val dao = new OrderDAO(dbContext)
      await(dao.insertOne(order))


      val orderFound: Order = await(dao.collection.find(Filters.equal("state", order.state)).toFuture()).head
      val modifiedUser = orderFound.copy(state = "otroState")

      await(dao.replaceOne(modifiedUser))
      await(dao.collection.count().toFuture()) mustBe 1


    }

  }

}
