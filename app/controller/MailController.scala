package controller

import com.google.inject.Inject
import controller.util.BaseController
import service.{CacheService, MailService}

import scala.concurrent.{ExecutionContext, Future}

class MailController @Inject()(service: MailService)(implicit ec: ExecutionContext) extends BaseController{

  def devices(id: Option[String]) = AsyncAction { implicit request =>
    service.device(id).map { res =>
      Ok(res)
    }.recover {
      case ex: Exception => constructErrorResult(ex.getMessage, ex)
    }
  }

  def sendEmail(id: String, typeMail: String) = AsyncAction { implicit request =>
    service.sendEmail(id, typeMail).map { res =>
      Ok(res)
    }.recover {
      case ex: Exception => constructErrorResult(ex.getMessage, ex)
    }
  }

  def activate(status: Boolean) = AsyncAction { implicit request =>
    Future(Ok(Map("result" -> service.activateEmail(status))))
  }

}
