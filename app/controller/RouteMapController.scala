package controller
import com.google.inject.Inject
import controller.util.BaseController
import play.api.libs.ws._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

case class Geolocation(x:String ,y:String)

class RouteMapController @Inject() (ws: WSClient)(implicit ec: ExecutionContext) extends BaseController {

  def findRouteMap = AsyncActionWithBody[Array[Geolocation]] { implicit r =>

    val apiKey = "AIzaSyDVVJd5t5m0aIBMNIo4q6NK6Dnr5-nWVfM"
    val request: WSRequest = ws.url(s"https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=Washington,DC&destinations=New+York+City,NY&key=$apiKey")

     //request.get().map{response=>Ok(response.body[Route].get.toString.parseJsonTo)}

    Future(Ok())

  }
}