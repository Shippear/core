package controller.util

import common.Logging
import common.serialization.{SnakeCaseJsonProtocol,_}
import play.api.mvc.{InjectedController, Request}
import play.api.mvc._

import scala.util.{Failure, Success, Try}

class BaseController extends InjectedController with Logging with SnakeCaseJsonProtocol {

  implicit def pimpRequest(request: Request[AnyContent]): PimpRequest = new PimpRequest(request)

  class PimpRequest private[controller](request: Request[AnyContent]) {
    def parseBodyTo[T: Manifest]: T = {
      implicit val headers = new Headers {
        override def headers: Map[String, String] = request.headers.toSimpleMap
      }
      Try{request.body.asJson.get.toString.parseJsonTo[T]} match {
        case Failure(ex) => error(s"Error parsing from json to ${manifest.toString()}", ex); throw ex
        case Success(some) => some
      }
    }
  }


}

trait Headers {

  def headers: Map[String, String]

  def getOrElse(key: String, another: String) = headers.getOrElse(key, another)

}
