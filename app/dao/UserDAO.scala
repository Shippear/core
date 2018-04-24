package dao

import ai.snips.bsonmacros.{BaseDAO, CodecGen, DatabaseContext}
import com.google.inject.Inject
import database.Mongo
import model.User
import org.mongodb.scala.MongoCollection

import scala.concurrent.ExecutionContext

class UserDAO @Inject()(mongoClient: Mongo, dbContext: DatabaseContext)(implicit ec: ExecutionContext) extends BaseDAO[User]{
  CodecGen[User](dbContext.codecRegistry)

  override def collection: MongoCollection[User] = dbContext.database(mongoClient.config.database).getCollection[User]("users")
}
