package controller.util

import com.google.inject.Inject
import common.serialization.{SnakeCaseJsonProtocol, _}
import common.{ConfigReader, Logging}
import play.api.mvc.{InjectedController, Request, _}
import service.Exception.{ShippearException, NotFoundException}

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
      request.body.asJson.get.toString.parseJsonTo[T]
    }
  }


  def AsyncAction(block: ShippearRequest[AnyContent] => Future[Result]) = Action.async { request =>
      doRequest(request, _ => ShippearRequest(request.body, request.headers.toSimpleMap), block)
  }


  def AsyncActionWithBody[B : Manifest](block: ShippearRequest[B] => Future[Result]) = Action.async { request =>
    val parserBody = (req: Request[AnyContent]) => {
      Try {
        req.parseBodyTo[B]
      } match {
        case Failure(ex) =>
          val msg = s"Error parsing from json to ${manifest.toString()}"
          error(msg, ex)
          throw ParseBodyException(s"$msg. ${ex.getMessage}", ex)
        case Success(some) => ShippearRequest(some, req.headers.toSimpleMap)
      }
    }

    doRequest(request, parserBody, block)

  }

  protected def doRequest[B: Manifest](request: Request[AnyContent], parserBody: Request[AnyContent] => ShippearRequest[B], block: ShippearRequest[B] => Future[Result]) = {
    Try {
      if (verifyApiKey(request.headers.toSimpleMap))
        block(parserBody(request))
      else
        Future(Unauthorized("Invalid API_KEY"))
    } match {
      case Success(result) => result
      case Failure(ex) => Future(BadRequest(Map("result" -> ex.getMessage)))
    }
  }

  protected def verifyApiKey(headers: Map[String, String]): Boolean = {
    val apiKey = headers.get(ShippearHeaders.API_KEY)

    apiKey.isDefined && apiKey.get.equals(key)
  }


  protected def constructErrorResult(message: String, ex: Exception) = {
    error(message, ex)
    ex match {
      case NotFoundException(msg) => NotFound(Map("result" -> s"$msg. ${ex.getMessage}"))
      case _: IllegalArgumentException | _: ShippearException => BadRequest(Map("result" -> s"$message. ${ex.getMessage}"))
      case _ => InternalServerError(Map("result" -> s"$message. ${ex.getMessage}"))
    }

  }

}

trait Headers {

  def headers: Map[String, String]

  def getOrElse(key: String, another: String) = headers.getOrElse(key, another)

}

case class ShippearRequest[T](content: T, headers: Map[String,String]) extends Headers
