package dao

import ai.snips.bsonmacros.DatabaseContext
import dao.embbebedmongo.{MongoTest, ToDocument}
import model._
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global


class UserDAOTest extends MongoTest with ToDocument[User] with MockitoSugar {

  val contactInfo = ContactInfo("email", "phone")

  val addressGeolocation = Geolocation(132, -123)
  val address = Address(addressGeolocation, "alias", "street", 123, "zipCode", Some("appart"),2)
  val paymentMethod = PaymentMethod("cardOwner","cardnumber", "12/02/1992", "securityCode", "cardType")

  val user = User("id", "onesignalId", "user.name", "firstName", "lastName",
  "dni1234", contactInfo, "photUrl", Seq(address), None, Seq(paymentMethod))


  "UserDAO" should {
    "save" in{

      val dao = new UserDAO(mock[DatabaseContext])

      await(dao.collection.insertOne(user).toFuture())
      await(dao.collection.count().toFuture()) mustBe 1

    }

  }

}
