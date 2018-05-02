package service


import com.google.inject.Inject
import common.Logging
import dao.UserDAO
import model.User
import org.mongodb.scala.bson.collection.immutable.Document
import play.api.libs.json.{Json, Writes}

import scala.concurrent.ExecutionContext

class UserService @Inject()(val dao: UserDAO)(implicit ec: ExecutionContext) extends Logging
  with Service[User]{



  //Esto puede no estar, peeeero, para que lo tengan de ejemplo
}
