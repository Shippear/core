package controller

import com.google.inject.Inject
import controller.util.BaseController
import model.CacheGeolocation
import service.CacheService
import service.Exception.{InternalServerErrorException, NotFoundException}

import scala.concurrent.{ExecutionContext, Future}

class CacheController @Inject()(service: CacheService)(implicit ec: ExecutionContext) extends BaseController {

  def updateLocation = AsyncActionWithBody[CacheGeolocation]{ implicit request =>
    service.updateLocation(request.content)
    Future(Ok(Map("result" -> s"User ${request.content._id} was successfully updated.")))
  }

  def geolocation(idUser: String) = AsyncAction { implicit request =>
    service.geolocation(idUser) match {
      case Some(result) => Future(Ok(result))
      case _ => val msg = s"User $idUser not found"
        Future(constructErrorResult(msg, NotFoundException(msg)))
    }
  }

  def active(value: Boolean) = {
    val newValue = service.active(value)
    Future(Map("result" -> s"Saving cache to DB now is: $newValue"))
  }

}
