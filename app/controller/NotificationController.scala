package controller

import com.google.inject.Inject
import controller.util.BaseController
import service.{CacheService, NotificationService}

import scala.concurrent.{ExecutionContext, Future}

class NotificationController @Inject()(service: NotificationService)(implicit ec: ExecutionContext) extends BaseController {

  def sendEmail(id: String, typeMail: String) = AsyncAction { implicit request =>
   /* service.sendEmail(id, typeMail).map { res =>
      Ok(res)
    }.recover {
      case ex: Exception => constructErrorResult(ex.getMessage, ex)
    }

    */

    Future(Ok("a"))
  }

  def sendPush(id: String, typeMail: String) = AsyncAction { implicit request =>
    /*
    service.sendNotification(id, typeMail).map { res =>
      Ok(res)
    }.recover {
      case ex: Exception => constructErrorResult(ex.getMessage, ex)
    }
    */

    Future(Ok("a"))
  }


  def activatePush(status: Boolean) = AsyncAction { implicit request =>
    Future(Ok(Map("result" -> service.activatePush(status))))
  }

  def activateMail(status: Boolean) = AsyncAction { implicit request =>
    Future(Ok(Map("result" -> service.activateMail(status))))
  }

}
