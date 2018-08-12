package controller

import com.google.inject.Inject
import controller.util.BaseController
import model.internal.CacheGeolocation
import service.Exception.NotFoundException
import service.{CacheService, CancelOrderService}

import scala.concurrent.{ExecutionContext, Future}

class TaskController @Inject()(cacheService: CacheService,
                               cancelService: CancelOrderService)(implicit ec: ExecutionContext) extends BaseController {

  def updateLocation = AsyncActionWithBody[CacheGeolocation]{ implicit request =>
    cacheService.updateLocation(request.content)
    Future(Ok(Map("result" -> s"User ${request.content._id} was successfully updated.")))
  }

  def geolocation(idUser: String) = AsyncAction { implicit request =>
    cacheService.geolocation(idUser) match {
      case Some(result) => Future(Ok(result))
      case _ => val msg = s"User $idUser not found"
        Future(constructErrorResult(msg, NotFoundException(msg)))
    }
  }

  def activeCache(status: Boolean) = AsyncAction { implicit request =>
    Future(Ok(Map("result" -> s"Saving cache to DB now is: ${cacheService.active(status)}")))
  }

  def activeCancel(status: Boolean) = AsyncAction { implicit request =>
    Future(Ok(Map("result" -> s"Cancel orders task is: ${cancelService.active(status)}")))
  }

}
