package service

import ai.snips.bsonmacros.BaseDAO
import common.serialization.CamelCaseJsonProtocol
import org.mongodb.scala.bson.collection.immutable.Document
import play.api.libs.json.{Json, Writes}

trait Service[T] extends CamelCaseJsonProtocol {

  def snake2camel(in: String) = {
    if(in.toUpperCase.equals("_ID"))
      "_id"
    else
      "_([a-z\\d])".r.replaceAllIn(in, _.group(1).toUpperCase)
  }


  def dao: BaseDAO[T]

  implicit def toDoc(doc: T)(implicit writes: Writes[T]): Document =
    Document(Json.stringify(Json.toJson(doc)))

  def create(doc: T) = {
    dao.insertOne(doc)
  }

  def findBy(params: Map[String, String]) = {
    val criteria = params.map {case (a, b) => (snake2camel(a), b)}
    dao.findOne(Document(criteria))
  }

  def update(doc: T) = {
    dao.replaceOne(doc)
  }

  def all = dao.all.toFuture()
}
