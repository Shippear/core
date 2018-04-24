package controller

import com.google.inject.Inject
import controller.util.BaseController
import controllers.Assets
import controller.util.BaseController
import play.api.Configuration
import play.api.http.HttpErrorHandler
import play.api.mvc.{Action, AnyContent}

class FrontendController @Inject()(assets: Assets,
                                   errorHandler: HttpErrorHandler,
                                   config: Configuration) extends BaseController {

  def index: Action[AnyContent] = assets.at("index.html")

  def assetOrDefault(resource: String): Action[AnyContent] = if (resource.startsWith(config.get[String]("apiPrefix"))){
    Action.async(r => errorHandler.onClientError(r, NOT_FOUND, "Not found"))
  } else {
    if (resource.contains(".")) assets.at(resource) else index
  }

}