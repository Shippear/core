package dao.embbebedmongo

import ai.snips.bsonmacros.DatabaseContext
import org.mongodb.scala.bson.Document
import org.mongodb.scala.{MongoClient, MongoCollection}

trait MongoInit {
  self: Connection =>

  lazy val client = MongoClient(config.uri)

  def dbCollection(name: String): MongoCollection[Document] = client.getDatabase(config.database).getCollection(name)

}
