package controller

import com.google.inject.Inject
import controller.util.BaseController
import model.request.RouteRequest
import service.RoutePriceService

import scala.concurrent.ExecutionContext

class RouteMapController @Inject()(service: RoutePriceService)(implicit ec: ExecutionContext) extends BaseController {

  def priceInformation = AsyncActionWithBody[RouteRequest] { implicit request =>
    service.priceInformation(request.content)
      .map(Ok(_))
      .recover {
      case ex: Exception =>
        constructErrorResult(s"Error getting price information for ${request.content.userName}", ex)
    }
  }
}