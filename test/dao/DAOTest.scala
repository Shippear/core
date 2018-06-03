package dao

import java.io.{FileOutputStream, OutputStream}
import java.nio.file.Files
import java.util.Date

import dao.embbebedmongo.MongoTest
import dao.util.ShippearDAO
import javax.imageio.ImageIO
import model._
import model.internal._
import org.joda.time.DateTime
import org.mongodb.scala.model.Filters
import play.api.test.Helpers.{await, _}
import qrcodegenerator.QrCodeGenerator
import repository.ShippearRepository
import service.Exception.NotFoundException
import OrderState._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.tools.nsc.classpath.FileUtils


class DAOTest extends MongoTest with ShippearRepository[Order] {

  override def collectionName: String = "test"

  lazy val dao: ShippearDAO[Order] = new ShippearDAO[Order](collectionName, dbContext)

  val originGeolocation = Geolocation(132, -123)
  val originCity = City(2, "Almagro")
  val origin = Address(originGeolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), originCity , public = true)
  val destinationGeolocation = Geolocation(132, -123)
  val destinationCity = City(1, "Nuñez")
  val destination = Address(destinationGeolocation, Some("alias"), "aaaaaaa", 1231231, "zipCode", Some("appart"), destinationCity, public = true)
  val route = Route(origin, destination)

  lazy val participantId = "11111"
  val order = Order("123", "12345", participantId, Some("carrierId"),
    PENDING_PICKUP, "operationType", route, new Date, new Date, Some(new Date), Some(new Date), None)

  val qrCodeGenerator = new QrCodeGenerator
  val qrCode = qrCodeGenerator.generateQrImage("123").stream().toByteArray
  val orderWithQrCode = Order("123", "12345", participantId, Some("carrierId"),
    "state", "operationType", route, new Date, new Date, Some(new Date), Some(new Date), Some(qrCode))

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

    "Save an order with QR code" in {
      await(dao.insertOne(orderWithQrCode))
      await(dao.all).size mustBe 1
    }


  }
}
