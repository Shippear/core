package controller


import com.google.inject.Inject
import controller.util.BaseController
import model.User
import service.UserService

import scala.concurrent.ExecutionContext

class UserController @Inject()(service: UserService)(implicit ec: ExecutionContext) extends BaseController {

  def createUser = Action.async { implicit request =>
    val user = request.parseBodyTo[User]
    service.save(user)
      .map {
        _ => Ok(s"User ${user.userName} created successfully")
      }
      .recover {
        case ex: Exception =>
          error("Error creating a new user", ex)
          InternalServerError(s"Error creating a new user ${ex.getMessage}")
    }
  }

  def findUser(userName: String) = Action.async { implicit request =>
    service.user(userName)
      .map {
        user => Ok(user)
      }
      .recover {
        case ex: Exception =>
          error(s"Error getting user $userName", ex)
          InternalServerError(s"Error creating a new user ${ex.getMessage}")
      }
  }

}