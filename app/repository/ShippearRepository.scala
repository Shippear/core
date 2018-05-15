package repository

import com.google.inject.Inject
import common.Logging
import dao.util.{ShippearDAO, ShippearDAOFactory}
import org.mongodb.scala.bson.collection.immutable.Document
import play.api.libs.json.{Json, Writes}

import scala.concurrent.Future

trait ShippearRepository[T] extends Logging {

  def snake2camel(in: String) = {
    if(in.toUpperCase.equals("_ID"))
      "_id"
    else
      "_([a-z\\d])".r.replaceAllIn(in, _.group(1).toUpperCase)
  }

  implicit def object2Document(obj: T)(implicit writes: Writes[T]): Document =
    Document(Json.stringify(Json.toJson(obj)))


  protected def replace[A](list: Seq[A], elem: A, criteria: A => Boolean): Seq[A] =
    list.filter(criteria) :+ elem

  implicit def criteria2Document(params: Map[String, String]): Document = {
    val criteria = params.map {case (a, b) => (snake2camel(a), b)}
    Document(criteria)
  }


  @Inject() var daoFactory: ShippearDAOFactory = _

  def collectionName: String

  def dao: ShippearDAO[T]

  def create(doc: T) = {
    info(s"Creating in collection $collectionName.")
    dao.insertOne(doc)
  }

  def findBy(params: Map[String, String]) = {
    info(s"FindBy criteria ${params.mkString(", ")}.")
    dao.findOne(params)
  }

  def findOneById(id: String): Future[T] = {
    info(s"FindOneById $id.")
    dao.findOneById(id)
  }

  def update(doc: T) = {
    info(s"Updating to collection $collectionName")
    dao.replaceOne(doc)
  }

  def all = dao.all
}