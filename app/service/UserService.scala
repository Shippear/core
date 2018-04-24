package service

import com.google.inject.Inject
import common.Logging
import dao.UserDAO
import model.User
import org.bson
import org.mongodb.scala.bson
import org.mongodb.scala.bson.collection.immutable.Document

import scala.concurrent.{ExecutionContext, Future}

class UserService @Inject()(dao: UserDAO)(implicit ec: ExecutionContext) extends Logging {

  def save(user: User): Future[_] = {
    val userDB = User(userName = user.userName, firstName = user.firstName, lastName = user.lastName, photoId = user.photoId)

    dao.insertOne(userDB)
  }


  def user(userName: String) = {
    dao.findOne(Document("userName" -> userName))
  }

}
