package common

import akka.stream.Materializer
import com.google.inject.Inject
import play.api.mvc._
import controller.util.ShippearHeaders._

import scala.concurrent.{ExecutionContext, Future}

class LoggingFilter @Inject()(implicit val mat: Materializer, exec: ExecutionContext) extends Filter with Logging {

  val debugRoutes = List("/health-check")

  def apply(next: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {

    val UNKNOWN = "UNKNOWN"
    val headers = request.headers.toSimpleMap
    implicit val context = FilterContext(headers)

    val startTime = System.currentTimeMillis
    log(s"START X-UOW: [${headers.getOrElse(X_UOW, UNKNOWN)}] - ${request.method} ${request.uri}")

    next(request).map { result =>

      val requestTime = System.currentTimeMillis() - startTime
      log(s"END X-UOW: [${headers.getOrElse(X_UOW, UNKNOWN)}] - ${request.method} ${request.uri} took $requestTime ms and returned ${result.header.status}")
      result.withHeaders("Request-Time" -> requestTime.toString)
    }

  }

  def log(message: String): Unit = message match {
    case _ if debugRoutes.exists(message.contains(_)) => debug(message)
    case _ => info(message)
  }

  case class FilterContext(headers: Map[String, String])

}
