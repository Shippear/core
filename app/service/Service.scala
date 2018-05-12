package service

import ai.snips.bsonmacros.BaseDAO
import common.serialization.CamelCaseJsonProtocol
import org.mongodb.scala.bson.collection.immutable.Document
import play.api.libs.json.{Json, Writes}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

trait Service[T] extends CamelCaseJsonProtocol {

  def snake2camel(in: String) = {
    if(in.toUpperCase.equals("_ID"))
      "_id"
    else
      "_([a-z\\d])".r.replaceAllIn(in, _.group(1).toUpperCase)
  }

  protected def replace[A](list: Seq[A], elem: A, criteria: A => Boolean): Seq[A] =
    list.filter(criteria) :+ elem

  implicit def unwrapFuture[A](future: Future[A]): A = {
    Await.result(future, Duration.Inf)
  }

  def dao: BaseDAO[T]

  implicit def toDoc(obj: T)(implicit writes: Writes[T]): Document =
    Document(Json.stringify(Json.toJson(obj)))

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
