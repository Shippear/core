package database

case class MongoConfiguration(database: String, port: Option[Int], uri: String)
