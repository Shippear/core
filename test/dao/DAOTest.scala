package dao

import java.util.Date

import common.DateTimeNow._
import dao.util.ShippearDAO
import embbebedmongo.MongoTest
import model.internal.OperationType._
import model.internal.OrderState._
import model.internal._
import model.internal.price.enum.{Size, Weight}
import org.joda.time.DateTime
import org.mongodb.scala.model.Filters
import play.api.test.Helpers.{await, _}
import qrcodegenerator.QrCodeGenerator
import repository.ShippearRepository
import service.Exception.NotFoundException

import scala.concurrent.ExecutionContext.Implicits.global


class DAOTest extends MongoTest with ShippearRepository[Order] {

  override def collectionName: String = "test"

  lazy val dao: ShippearDAO[Order] = new ShippearDAO[Order](collectionName, dbContext)

  val originGeolocation = Geolocation(132, -123)
  val originCity = City(2, "Almagro")
  val origin = Address(originGeolocation, Some("alias"), "street", 123, "zipCode", Some("appart"), originCity , public = true, None, None)
  val destinationGeolocation = Geolocation(132, -123)
  val destinationCity = City(1, "Nuñez")
  val destination = Address(destinationGeolocation, Some("alias"), "aaaaaaa", 1231231, "zipCode", Some("appart"), destinationCity, public = true, None, None)
  val route = Route(origin, destination, None)

  val birthDate = rightNowTime
  val contactInfo = ContactInfo("email@email.com", "011123119")
  val applicantData = UserDataOrder("12345", "name", "last", birthDate, contactInfo, "photo", "notification", Some(0), Some(SENDER))
  lazy val participantId = "11111"
  val participantData = UserDataOrder(participantId, "name", "last", birthDate, contactInfo,  "photo", "notification", Some(0), Some(RECEIVER))
  val visa = PaymentMethod("ownerName", "123", Some("cardCode"), Some("bankCode"), "02/20", "securityCode", Some("VISA"))

  val order = Order("123", applicantData, participantData, None, None, 123, "description",
    PENDING_PICKUP, "operationType", Size.SMALL, Weight.HEAVY, List(TransportType.MOTORCYCLE), route, new Date,
    new Date, Some(new Date), None, None, None, None, visa, 0, Some(0), None)

  val qrCodeGenerator = new QrCodeGenerator
  val qrCode = qrCodeGenerator.generateQrImage("123").stream().toByteArray
  val orderWithQrCode = Order("123", applicantData, participantData, None, None, 123, "description",
    "state", "operationType", Size.SMALL, Weight.HEAVY, List(TransportType.MOTORCYCLE), route, new Date,
    new Date, Some(new Date), Some(qrCode), Some("qrUrl"), None, None, visa, 0, Some(0), None)

  "OrderDAO" should{
    "Save an object" in {
      await(dao.insertOne(order))
      await(dao.all).size mustBe 1
    }

    "Update an object" in {
      order.participant.id mustBe participantId
      await(dao.all).size mustBe 0
      await(dao.insertOne(order))
      await(dao.all).size mustBe 1
      val originalOrder = await(dao.findOneById(order._id) )

      val modifiedOrder = originalOrder.copy(description = "other")
      await(dao.updateOne(modifiedOrder))

      await(dao.all).size mustBe 1
      val foundModified = await(dao.findOneById(order._id))
      foundModified.description mustEqual "other"
      foundModified.size mustEqual originalOrder.size

    }

    "Replace an object" in {
      order.participant.id mustBe participantId
      await(dao.all).size mustBe 0
      await(dao.insertOne(order))
      await(dao.all).size mustBe 1
      val orderFound = await(dao.findOneById(order._id) )

      val newParticipant = participantData.copy(id = "bla")
      val newOrder = orderFound.copy(participant = newParticipant)
      await(dao.replaceOne(newOrder))
      val newOrderFound = await(dao.findOneById(order._id))
      newOrderFound.participant.id mustBe "bla"
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
      await(dao.findByFilters(Filters.eq("participant.id", participantId))).size mustBe 1
      await(dao.findByFilters(Filters.eq("participant.id", "AAAA"))).size mustBe 0

      //greater than & lower than
      val availableFrom: Date = new DateTime().minusDays(1)
      await(dao.findByFilters(Filters.gt("availableFrom", availableFrom))).size mustBe 1
      await(dao.findByFilters(Filters.lt("availableFrom", availableFrom))).size mustBe 0

      //using and
      val filterAnd = Filters.and(Filters.eq("participant.id", participantId), Filters.lt("availableFrom", availableFrom))
      await(dao.findByFilters(filterAnd)).size mustBe 0

      //using or
      val filterOr = Filters.or(Filters.eq("participant.id", participantId), Filters.lt("availableFrom", availableFrom))
      await(dao.findByFilters(filterOr)).size mustBe 1
    }

    "Save an order with QR code" in {
      await(dao.insertOne(orderWithQrCode))
      val orderWithQR = await(dao.findOneById(orderWithQrCode._id))
      orderWithQR.qrCode.isDefined mustBe true
      await(dao.all).size mustBe 1
    }


  }
}
