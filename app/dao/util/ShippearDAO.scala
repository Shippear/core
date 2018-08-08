package dao.util

import ai.snips.bsonmacros
import ai.snips.bsonmacros.BsonMagnets.CanBeBsonValue
import com.google.inject.Inject
import common.{ConfigReader, Logging}
import dao.ShippearDBContext
import database.MongoConfiguration
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.bson.{BsonDocument, BsonDocumentReader, BsonDocumentWriter}
import org.bson.codecs.{Decoder, DecoderContext, Encoder, EncoderContext}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.FindObservable
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.bson.{BsonDocument, Document}
import org.mongodb.scala.model.{Filters, UpdateOptions}
import service.Exception.NotFoundException

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class ShippearDAO[T] @Inject()(collectionName: String, dbContext: ShippearDBContext)(implicit ct: ClassTag[T],
                                                             ec: ExecutionContext) extends ConfigReader with Logging{
  private val ID = "_id"
  private val config = envConfiguration.as[MongoConfiguration]("mongodb")
  private lazy val collection = dbContext.database(config.database).getCollection[T](collectionName)
  implicit val codecs = dbContext.codecRegistry

  private def bson(it: T) = bsonmacros.toDBObject(it)

  private def getId(it: T)= bson(it).get(ID)

  def byIdSelector(id: CanBeBsonValue): Document = Document(ID -> id.value)

  def find(bson: Document): FindObservable[T] = collection.find(bson)

  def findOneById(id: CanBeBsonValue): Future[T] = findOne(byIdSelector(id))

  def findOne(document: Document): Future[T] = {
    find(document).limit(1).toFuture.map(_.headOption).map{
      case Some(result) => result
      case _ => throw NotFoundException(s"Document not found in collection $collectionName for params: ${document.entrySet()}")
    }
  }

  def findByFilters(filters: Bson): Future[Seq[T]] =
    find(Document(filters.toBsonDocument(Filters.getClass, codecs))).toFuture


  def insertOne(it: T): Future[_] = collection.insertOne(it).toFuture()

  def updateOne(it: T): Future[_] = {
    val document = bson(it)
    collection.updateOne(byIdSelector(getId(it)), Document("$set" -> document)).toFuture()
  }

  def replaceOne(it: T): Future[_] = collection.replaceOne(byIdSelector(getId(it)), it).toFuture

  def upsertOne(it: T): Future[_] =  collection.replaceOne(byIdSelector(getId(it)), it, UpdateOptions().upsert(true)).toFuture

  def all: Future[Seq[T]] = collection.find().toFuture()

}

class ShippearDAOFactory @Inject()(dbContext: ShippearDBContext)(implicit ec: ExecutionContext) {
  def apply[T: ClassTag](collectionName: String): ShippearDAO[T] = new ShippearDAO[T](collectionName, dbContext)
}
