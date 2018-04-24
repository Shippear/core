package common

import akka.stream.Materializer
import com.google.inject.Inject
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class LoggingFilter @Inject()(implicit val mat: Materializer, exec: ExecutionContext) extends Filter with Logging {

  val debugRoutes = List("/health-check")

  def apply(next: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {

    implicit val context = FilterContext(request.headers.toSimpleMap)

    val startTime = System.currentTimeMillis
    log(s"START - ${request.method} ${request.uri}")

    next(request).map { result =>

      val requestTime = System.currentTimeMillis() - startTime
      log(s"END - ${request.method} ${request.uri} took $requestTime ms and returned ${result.header.status}")
      result.withHeaders("Request-Time" -> requestTime.toString)
    }

  }

  def log(message: String): Unit = message match {
    case _ if debugRoutes.exists(message.contains(_)) => debug(message)
    case _ => info(message)
  }

  case class FilterContext(headers: Map[String, String])

}
