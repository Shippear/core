package controller

import com.google.inject.Inject
import controller.util.BaseController
import model.User
import service.UserService
import scala.concurrent.ExecutionContext

class UserController @Inject()(service: UserService)(implicit ec: ExecutionContext) extends BaseController {

  def createUser = AsyncActionWithBody[User] { implicit request =>
    service.create(request.content).map { _ =>
      info(s"User ${request.content.userName} created")
      Ok(Map("result" -> s"${request.content.userName} created"))
    }.recover {
        case ex: Exception =>
          constructInternalError("Error creating a new user", ex)
      }
  }

  def findUser = AsyncActionWithBody[Map[String, String]] { implicit request =>
    service.findBy(request.content).map {
        user => if(user.isDefined) Ok(user) else NotFound(s"User with criteria ${request.content} not found")
      }.recover {
        case ex: Exception =>
          constructInternalError(s"Error getting user with criteria ${request.content}", ex)
      }
  }

  def updateUser = AsyncActionWithBody[User] { implicit request =>
    service.update(request.content).map{
      _ => Ok(s"User ${request.content.userName} updated successfully")
    }.recover {
    case ex: Exception =>
      constructInternalError(s"Error updating user ${request.content.userName}", ex)
    }
  }

  def allUsers = AsyncAction { implicit request =>
    service.all.map{ result =>
      Ok(result.toList)
    }.recover {
      case ex: Exception =>
        constructInternalError(s"Error getting all users", ex)
    }
  }


}
