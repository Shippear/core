package service

import com.google.inject.Inject
import database.Mongo
import model.User

class UserService @Inject() (mongoClient: Mongo){

  def toUser(name: String) = User(name, "last", "123")

}
