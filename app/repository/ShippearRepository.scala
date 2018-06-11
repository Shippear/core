package repository

import com.google.inject.Inject
import common.Logging
import common.serialization.{CamelCaseJsonProtocol, _}
import dao.util.{ShippearDAO, ShippearDAOFactory}
import org.mongodb.scala.bson.collection.immutable.Document

import scala.concurrent.Future

trait ShippearRepository[T] extends Logging with CamelCaseJsonProtocol{

  def snake2camel(in: String) = {
    if(in.toUpperCase.equals("_ID"))
      "_id"
    else
      "_([a-z\\d])".r.replaceAllIn(in, _.group(1).toUpperCase)
  }

  implicit def object2Document(obj: T): Document =
    Document(obj.toJson)


  protected def replaceOrAdd[A](list: Seq[A], elem: A)(predicate: A => Boolean): Seq[A] =
    list.filterNot(predicate) :+ elem

  implicit def criteria2Document(params: Map[String, String]): Document = {
    val criteria = params.map {case (a, b) => (snake2camel(a), b)}
    Document(criteria)
  }


  @Inject() var daoFactory: ShippearDAOFactory = _

  def collectionName: String

  def dao: ShippearDAO[T]

  def create(doc: T) = dao.insertOne(doc)

  def findBy(params: Map[String, String]) = dao.findOne(params)

  def findOneById(id: String): Future[T] = dao.findOneById(id)

  def update(doc: T) = dao.replaceOne(doc)

  def all = dao.all
}