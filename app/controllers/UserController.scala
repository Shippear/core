package controllers

import com.google.inject.Inject
import controllers.util.BaseController
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

class UserController @Inject()(firebaseClient: WSClient, ec: ExecutionContext) extends BaseController {

  def test = Action.async { implicit request =>
    firebaseClient.url("https://httpbin.org/ip").get.map {
      response => Ok(response.body)
    }
  }

}
