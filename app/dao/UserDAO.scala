package dao

import ai.snips.bsonmacros.BaseDAO
import com.google.inject.Inject
import common.ConfigReader
import database.MongoConfiguration
import model._
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.mongodb.scala.MongoCollection

import scala.concurrent.ExecutionContext

class UserDAO @Inject()(dbContext: DBContext)(implicit ec: ExecutionContext)
  extends BaseDAO[User] with ConfigReader {

  val config = envConfiguration.as[MongoConfiguration]("mongodb")

  override def collection: MongoCollection[User] = dbContext.database(config.database).getCollection[User]("users")
}
