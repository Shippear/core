package controller

import com.google.inject.Inject
import controller.util.BaseController
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

class HomeController @Inject()(implicit ec: ExecutionContext)extends BaseController {

  def appSummary = Action {
    Ok(Json.obj("content" -> "Scala Play React Seed"))
  }
}
