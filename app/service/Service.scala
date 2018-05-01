package service

import ai.snips.bsonmacros.BaseDAO
import org.mongodb.scala.bson.collection.immutable.Document

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Service[T] {

  def dao: BaseDAO[T]

  def create(doc: T): Future[String] = {
    dao.insertOne(doc).map(result => result.toString)
  }

  def retrieve(paramName: String, paramValue: String) = {
    dao.findOne(Document(paramName -> paramValue))
  }

  def update(doc: T) = {
    dao.replaceOne(doc)
  }
}
