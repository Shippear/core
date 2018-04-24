package controller

import controller.util.BaseController
import play.api.libs.json.Json

class HomeController extends BaseController {

  def appSummary = Action {
    Ok(Json.obj("content" -> "Scala Play React Seed"))
  }
}
