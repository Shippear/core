package controllers

import com.google.inject.Inject
import controllers.util.BaseController
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

class HomeController @Inject()(client: WSClient) extends BaseController {

  def appSummary = Action {
    info("Loggin TEST!!!")
    Ok(Json.obj("content" -> "Scala Play React Seed"))
  }
}
