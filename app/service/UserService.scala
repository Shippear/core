package service


import com.google.inject.Inject
import common.Logging
import dao.UserDAO
import model.{Order, User}
import play.api.mvc._
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.collection.immutable.Document

import scala.concurrent.{ExecutionContext, Future}

class UserService @Inject()(dao: UserDAO)(implicit ec: ExecutionContext) extends Logging {

  def save(user: User): Future[String] = {
    val userDB = user.copy(_id = new ObjectId().toHexString)
    dao.insertOne(userDB).map { _ => userDB._id }
  }


  def user(userName: String) = {
    dao.findOne(Document("userName" -> userName))
  }

  def update(user: User) = {
    dao.replaceOne(user)
  }

}
