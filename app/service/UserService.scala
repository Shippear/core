package service

import model.User

class UserService {

  def toUser(name: String) = User(name, "last", "123")

}
