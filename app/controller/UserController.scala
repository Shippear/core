package controller


import com.google.inject.Inject
import controller.util.BaseController
import model.User
import play.api.libs.ws.WSClient
import service.UserService

import scala.concurrent.ExecutionContext.Implicits.global

class UserController @Inject()(service: UserService) extends BaseController {

  //Todo action.async
  def createUser = Action { implicit request =>
    val user = request.parseBodyTo[User]
    Ok(service.toUser(user.name))
  }

}
