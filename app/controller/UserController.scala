package controller


import com.google.inject.Inject
import controller.util.BaseController
import model.User
import service.UserService
import scala.concurrent.ExecutionContext
class UserController @Inject()(service: UserService)(implicit ec: ExecutionContext) extends BaseController {

  def createUser = AsyncActionWithBody[User] { implicit request =>
    val user = request.content
    service.create(user).map { _ =>
      info(s"User ${user.userName} created")
        Ok(Map("result" -> s"${user.userName} created"))
      }.
      recover {
        case ex: Exception =>
          error("Error creating a new user", ex)
          InternalServerError(s"Error creating a new user ${ex.getMessage}")
    }
  }

  def findUser = AsyncActionWithBody[Map[String, String]] { implicit request =>
    val criteria = request.content.map {case (a, b) => (snake2camel(a), b)}

    service.findBy(criteria).map {
        user => Ok(user)
      }
      .recover {
        case ex: Exception =>
          error(s"Error getting user ", ex)
          InternalServerError(s"Error creating a new user ${ex.getMessage}")
      }
  }

  def updateUser = AsyncActionWithBody[User] { implicit request =>
    service.update(request.content).map{
      _ => Ok("User updated successfully")
    }.recover {

    case ex: Exception =>
      error(s"Error updating user ${request.content.userName}", ex)
      InternalServerError(s"Error creating a new user ${ex.getMessage}")
    }
  }

}
