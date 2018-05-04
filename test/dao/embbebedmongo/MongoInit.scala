package dao.embbebedmongo

import org.mongodb.scala.bson.Document
import org.mongodb.scala.{MongoClient, MongoCollection}

trait MongoInit {
  self: Connection =>

  lazy val client = MongoClient(s"mongodb://localhost:${network.getPort}")

  def dbCollection: MongoCollection[Document] = client.getDatabase("test").getCollection("model")

  def testObject: Document = Document("name" -> "test")

}
