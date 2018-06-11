package controller

import com.google.inject.Inject
import controller.util.BaseController
import model.request.RouteRequest
import service.RouteMapService

import scala.concurrent.ExecutionContext

class RouteMapController @Inject()(service: RouteMapService)(implicit ec: ExecutionContext) extends BaseController {

  def addressInformation = AsyncActionWithBody[RouteRequest] { implicit request =>
    service.addressInformation(request.content)
      .map(Ok(_))
      .recover {
      case ex: Exception =>
        constructErrorResult(s"Error getting address information for ${request.content.userName}", ex)
    }
  }
}