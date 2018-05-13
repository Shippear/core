package dao

import ai.snips.bsonmacros.{BaseDAO, CodecGen, DatabaseContext}
import com.google.inject.Inject
import common.ConfigReader
import database.MongoConfiguration
import model._
import org.mongodb.scala.MongoCollection
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

import scala.concurrent.ExecutionContext

class OrderDAO @Inject()(dbContext: ShippearDBContext)(implicit ec: ExecutionContext) extends BaseDAO[Order] with ConfigReader {

  val config = envConfiguration.as[MongoConfiguration]("mongodb")

  override def collection: MongoCollection[Order] = dbContext.database(config.database).getCollection[Order]("orders")
}
