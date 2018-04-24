package controller


import com.google.inject.Inject
import controller.util.BaseController
import model.User
import service.UserService

class UserController @Inject()(service: UserService) extends BaseController {

  //Todo action.async
  def createUser = Action { implicit request =>
    val user = request.parseBodyTo[User]
    Ok(service.toUser(user.name))
  }

}
