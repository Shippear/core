package dao

import ai.snips.bsonmacros.{BaseDAO, CodecGen, DatabaseContext}
import com.google.inject.Inject
import common.ConfigReader
import database.MongoConfiguration
import model.User
import org.mongodb.scala.MongoCollection
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

import scala.concurrent.ExecutionContext

class UserDAO @Inject()(dbContext: DatabaseContext)(implicit ec: ExecutionContext)
  extends BaseDAO[User] with ConfigReader {
  CodecGen[User](dbContext.codecRegistry)

  val config = envConfiguration.as[MongoConfiguration]("mongodb")

  override def collection: MongoCollection[User] = dbContext.database(config.database).getCollection[User]("users")
}
