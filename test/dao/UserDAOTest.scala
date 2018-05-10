package dao

import dao.embbebedmongo.{MongoTest, ToDocument}
import model._
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global


class UserDAOTest extends MongoTest with ToDocument[User] {

  val contactInfo = ContactInfo("email", "phone")

  val addressGeolocation = Geolocation(132, -123)
  val address = Address(addressGeolocation, "alias", "street", 123, "zipCode", Some("appart"),2, Some(true))
  val paymentMethod = PaymentMethod("cardOwner","cardnumber", "12/02/1992", "securityCode", "cardType", Some("cbu"))
  val transport = Transport("Auto ", Some("ABC123"), Some("Citroen c4"))

  val user = User("123", "onesignalId", "username", "firstName", "lastName",
  "dni1234", contactInfo, "photUrl", Seq(address), None, Seq(paymentMethod), Some(6.0), Some(transport))


  "UserDAO" should {
    "save" in {
      val dao = new UserDAO(dbContext)

      await(dao.insertOne(user))
      await(dao.collection.count().toFuture()) mustBe 1

      val otherUser = user.copy(_id = "otroId")
      await(dao.insertOne(otherUser))
      await(dao.collection.count().toFuture()) mustBe 2


    }

    "find" in {
      val dao = new UserDAO(dbContext)
      await(dao.insertOne(user))

      val bson: BsonDocument = Filters.equal("userName", user.userName).toBsonDocument(Filters.getClass, dbContext.dbContext.codecRegistry)
      val filter = Document(bson)

      await(dao.findOne(filter)).size mustBe 1
      await(dao.collection.find(Filters.equal("userName", "blablabla")).toFuture()).size mustBe 0

      await(dao.collection.find(Filters.gt("_id", 123)).toFuture()).size mustBe 0

      await(dao.collection.find(Filters.equal("contactInfo.email", user.contactInfo.email)).toFuture()).size mustBe 1

    }

    "update" in {
      val dao = new UserDAO(dbContext)
      await(dao.insertOne(user))


      val userFound: User = await(dao.collection.find(Filters.equal("userName", user.userName)).toFuture()).head
      val modifiedUser = userFound.copy(firstName = "otroFirstName")

      await(dao.replaceOne(modifiedUser))
      await(dao.collection.count().toFuture()) mustBe 1


    }

  }

}
