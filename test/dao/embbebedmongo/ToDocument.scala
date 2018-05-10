package dao.embbebedmongo

import org.mongodb.scala.bson.collection.immutable.Document
import play.api.libs.json.{Json, Writes}

trait ToDocument[T] {
  implicit def toDoc(obj: T)(implicit writes: Writes[T]): Document =
    Document(Json.stringify(Json.toJson(obj)))
}