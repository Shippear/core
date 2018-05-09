package dao

import ai.snips.bsonmacros.{BaseDAO, CodecGen, DatabaseContext}
import com.google.inject.Inject
import common.{ConfigReader, Logging}
import database.MongoConfiguration
import model._
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.mongodb.scala.MongoCollection

import scala.concurrent.ExecutionContext

class UserDAO @Inject()(dbContext: DatabaseContext)(implicit ec: ExecutionContext)
  extends BaseDAO[User] with ConfigReader {

  CodecGen[Geolocation](dbContext.codecRegistry)
  CodecGen[PaymentMethod](dbContext.codecRegistry)
  CodecGen[ContactInfo](dbContext.codecRegistry)
  CodecGen[Address](dbContext.codecRegistry)
  CodecGen[Order](dbContext.codecRegistry)
  CodecGen[User](dbContext.codecRegistry)

  val config = envConfiguration.as[MongoConfiguration]("mongodb")

  override def collection: MongoCollection[User] = dbContext.database(config.database).getCollection[User]("users")
}
