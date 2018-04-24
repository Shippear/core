package database

import common.ConfigReader
import org.mongodb.scala._
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

class Mongo extends ConfigReader {

  val config = envConfiguration.as[MongoConfiguration]("mongodb")

  private val mongoClient: MongoClient = MongoClient(config.uri)

  val database = mongoClient.getDatabase(config.database)
}
