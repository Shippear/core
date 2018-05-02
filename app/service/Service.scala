package service

import ai.snips.bsonmacros.BaseDAO
import common.serialization.CamelCaseJsonProtocol
import org.mongodb.scala.bson.collection.immutable.Document
import play.api.libs.json.{Json, Writes}

trait Service[T] extends CamelCaseJsonProtocol {

  def dao: BaseDAO[T]

  def toDoc(doc: T)(implicit writes: Writes[T]): Document = Document(Json.stringify(Json.toJson(doc)))

  def create(doc: T) = {
    dao.insertOne(doc)
  }

  def findBy(params: Map[String, String]) = {
    dao.findOne(Document(params))
  }

  def update(doc: T) = {
    dao.replaceOne(doc)
  }
}
