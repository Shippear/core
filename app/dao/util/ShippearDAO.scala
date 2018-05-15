package dao.util

import ai.snips.bsonmacros.BaseDAO
import ai.snips.bsonmacros.BsonMagnets.CanBeBsonValue
import com.google.inject.Inject
import common.{ConfigReader, Logging}
import dao.ShippearDBContext
import database.MongoConfiguration
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.mongodb.scala.bson.Document
import org.mongodb.scala.{FindObservable, MongoCollection}
import service.Exception.NotFoundException

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class ShippearDAO[T] @Inject()(collectionName: String, dbContext: ShippearDBContext)(implicit ct: ClassTag[T],
                                                             ec: ExecutionContext) extends ConfigReader with Logging{

  private val base = new BaseDAO[T] {
    override def collection: MongoCollection[T] = dbContext.database(config.database).getCollection[T](collectionName)
  }

  private val config = envConfiguration.as[MongoConfiguration]("mongodb")

  def findOneById(id: CanBeBsonValue): Future[T] =
    findOne(base.byIdSelector(id))

  def findOne(document: Document): Future[T] =
    base.findOne(document).map {
      case Some(value) => value
      case None =>
    }

  def updateOneById(id: CanBeBsonValue, update: Document): Future[_] = base.updateOneById(base.byIdSelector(id), update)

  def replaceOne(it: T): Future[_] = base.replaceOne(it)

  def insertOne(it: T): Future[_] = base.insertOne(it)

  def upsertOne(it: T): Future[_] = base.upsertOne(it)

  def all: Future[Seq[T]] = base.all.toFuture

  def find(bson: Document): FindObservable[T] = base.find(bson)

}

class ShippearDAOFactory @Inject()(dbContext: ShippearDBContext)(implicit ec: ExecutionContext) {
  def apply[T: ClassTag](collectionName: String): ShippearDAO[T] = new ShippearDAO[T](collectionName, dbContext)
}
