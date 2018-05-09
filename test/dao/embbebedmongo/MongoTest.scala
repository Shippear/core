package dao.embbebedmongo

import org.mongodb.scala.bson.collection.immutable.Document
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{Json, Writes}

import scala.concurrent.Await
import scala.concurrent.duration._

trait MongoTest extends PlaySpec with MongoInit with Connection with BeforeAndAfterEach with BeforeAndAfterAll {

  override def beforeAll = mongodExecutable.start

  override def afterAll = mongodExecutable.stop()

  override def afterEach = Await.result(client.getDatabase(config.database).drop().toFuture(), 30 seconds)

}


trait ToDocument[T] {
  implicit def toDoc(obj: T)(implicit writes: Writes[T]): Document =
    Document(Json.stringify(Json.toJson(obj)))
}