package controller

import com.google.inject.Inject
import controller.util.BaseController
import model.internal.User
import model.request.CarrierRating
import service.UserService

import scala.concurrent.ExecutionContext

class UserController @Inject()(service: UserService)(implicit ec: ExecutionContext) extends BaseController {

  def createUser = AsyncActionWithBody[User] { implicit request =>
    service.create(request.content).map { _ =>
      info(s"User ${request.content.userName} created")
      Ok(Map("result" -> s"${request.content.userName} created"))
    }.recover {
        case ex: Exception =>
          constructErrorResult("Error creating a new user", ex)
      }
  }

  def findUser = AsyncActionWithBody[Map[String, String]] { implicit request =>
    service.findOneBy(request.content).map {
        user => Ok(user)
      }.recover {
        case ex: Exception =>
          constructErrorResult(s"Error getting user with criteria ${request.content.mkString(", ")}", ex)
      }
  }

  def findUsers = AsyncActionWithBody[Map[String, String]] { implicit request =>
    service.findBy(request.content).map {
      users => Ok(users.toList)
    }.recover {
      case ex: Exception =>
        constructErrorResult(s"Error getting user with criteria ${request.content.mkString(", ")}", ex)
    }
  }

  def ordersByState(idUser: String) = AsyncAction { implicit request =>
    service.ordersByState(idUser).map {
      user => Ok(user)
    }.recover {
      case ex: Exception =>
        constructErrorResult(s"Error getting user _id $idUser", ex)
    }
  }


  def updateUser = AsyncActionWithBody[User] { implicit request =>
    service.update(request.content).map{
      _ => Ok(Map("result" -> s"User ${request.content.userName} updated successfully"))
    }.recover {
    case ex: Exception =>
      constructErrorResult(s"Error updating user ${request.content.userName}", ex)
    }
  }

  def allUsers = AsyncAction { implicit request =>
    service.all.map { result =>
      Ok(result.toList)
    }.recover {
      case ex: Exception =>
        constructErrorResult(s"Error getting all users", ex)
    }
  }
}
