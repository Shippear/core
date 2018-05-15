package controller.util

import com.google.inject.Inject
import common.serialization.{SnakeCaseJsonProtocol, _}
import common.{ConfigReader, Logging}
import play.api.mvc.{InjectedController, Request, _}
import service.Exception.NotFoundException

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


class BaseController @Inject()(implicit ec: ExecutionContext) extends InjectedController with Logging with SnakeCaseJsonProtocol with ConfigReader {

  val key = envConfiguration.getString(ShippearHeaders.API_KEY)

  implicit def pimpRequest(request: Request[AnyContent]): PimpRequest = new PimpRequest(request)

  class PimpRequest private[controller](request: Request[AnyContent]) {
    def parseBodyTo[T: Manifest]: T = {
      implicit val headers = new Headers {
        override def headers: Map[String, String] = request.headers.toSimpleMap
      }
      Try {
        request.body.asJson.get.toString.parseJsonTo[T]
      } match {
        case Failure(ex) => error(s"Error parsing from json to ${manifest.toString()}", ex); throw ex
        case Success(some) => some
      }
    }
  }


  def AsyncAction(block: ShippearRequest[AnyContent] => Future[Result]) = Action.async { request =>
    doRequest(ShippearRequest(request.body, request.headers.toSimpleMap), block)
  }

  def AsyncActionWithBody[B : Manifest](block: ShippearRequest[B] => Future[Result]) = Action.async { request =>
    doRequest(ShippearRequest(request.parseBodyTo[B], request.headers.toSimpleMap), block)
  }


  protected def doRequest[B](request: ShippearRequest[B], block: ShippearRequest[B] => Future[Result]): Future[Result] = {
    request.headers.get(ShippearHeaders.API_KEY) match {
      case Some(apiKey) => if(apiKey.equals(key)) block(request) else Future(Unauthorized("Invalid API_KEY"))
      case _ => Future(Unauthorized("Invalid API_KEY"))
    }
  }

  protected def constructErrorResult(message: String, ex: Exception) = {
    error(message, ex)
    ex match {
      case NotFoundException(msg) => NotFound(s"$message. $msg")
      case _ => InternalServerError(Map("result" -> s"$message. ${ex.getMessage}"))
    }

  }

}

trait Headers {

  def headers: Map[String, String]

  def getOrElse(key: String, another: String) = headers.getOrElse(key, another)

}

case class ShippearRequest[T](content: T, headers: Map[String,String]) extends Headers
