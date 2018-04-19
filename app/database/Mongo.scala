package database

import common.ConfigReader
import org.mongodb.scala._

class Mongo extends ConfigReader {

  private val mongoClient: MongoClient = MongoClient()
/*
  val database = {
    mongoClient.
    mongoClient.getDatabase()
  }

  def collection(name: String) = database.getCollection(name)
*/
}
