package dao.util

import ai.snips.bsonmacros
import ai.snips.bsonmacros.BaseDAO
import ai.snips.bsonmacros.BsonMagnets.CanBeBsonValue
import com.google.inject.Inject
import common.{ConfigReader, Filters, Logging}
import dao.ShippearDBContext
import database.MongoConfiguration
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.bson.{BsonDocument, Document}
import org.mongodb.scala.model.Filters
import org.mongodb.scala.{FindObservable, MongoCollection}
import service.Exception.NotFoundException

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class ShippearDAO[T] @Inject()(collectionName: String, dbContext: ShippearDBContext)(implicit ct: ClassTag[T],
                                                             ec: ExecutionContext) extends ConfigReader with Logging{

  private val base = new BaseDAO[T] {
    lazy val collection: MongoCollection[T] = dbContext.database(config.database).getCollection[T](collectionName)

    def getId(doc: Document) = bsonmacros.toDBObject(doc).get("_id").asString()
  }

  private val config = envConfiguration.as[MongoConfiguration]("mongodb")

  def findOneById(id: CanBeBsonValue): Future[T] =
    findOne(base.byIdSelector(id))

  def findOne(document: Document): Future[T] = {
   base.findOne(document).map{
      case Some(result) => result
      case _ => throw NotFoundException(s"Document not found in collection $collectionName for id ${base.getId(document)}")
    }
  }

  def updateOneById(id: CanBeBsonValue, update: Document): Future[_] = base.updateOneById(base.byIdSelector(id), update)

  def replaceOne(it: T): Future[_] = base.replaceOne(it)

  def insertOne(it: T): Future[_] = base.insertOne(it)

  def upsertOne(it: T): Future[_] = base.upsertOne(it)

  def all: Future[Seq[T]] = base.all.toFuture

  def find(bson: Document): FindObservable[T] = base.find(bson)

  def findByFilters(filters: Bson): Future[Seq[T]] =
    base.find(Document(filters.toBsonDocument(Filters.getClass, dbContext.dbContext.codecRegistry))).toFuture


}

class ShippearDAOFactory @Inject()(dbContext: ShippearDBContext)(implicit ec: ExecutionContext) {
  def apply[T: ClassTag](collectionName: String): ShippearDAO[T] = new ShippearDAO[T](collectionName, dbContext)
}
