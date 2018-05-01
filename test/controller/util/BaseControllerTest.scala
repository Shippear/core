package controller.util

import common.ConfigReader
import controller.UserController
import org.scalatestplus.play.PlaySpec
import play.api.mvc.{Result, Results}
import play.api.test.Helpers.status
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class BaseControllerTest extends PlaySpec with Results with ConfigReader {

  val apikey = envConfiguration.getString(ShippearHeaders.API_KEY)

  class FakeController extends BaseController {
    def action = AsyncAction { implicit request => Future(Ok("Hi!"))}
  }

  val fakeController = new FakeController
  fakeController.setControllerComponents(Helpers.stubControllerComponents())
  val fakeRequest = FakeRequest()


  "API_KEY Validation" should {

    "return a 200 when a VALID API_KEY header is present" in {
      val headers = (ShippearHeaders.API_KEY, apikey)
      val result: Future[Result] = fakeController.action.apply(fakeRequest.withHeaders(headers))

      status(result) mustBe 200

    }

    "return a 401 when an INVALID API_KEY header is present" in {
      val headers = (ShippearHeaders.API_KEY, "BLA BLA BLA")
      val result: Future[Result] = fakeController.action.apply(fakeRequest.withHeaders(headers))

      status(result) mustBe 401

    }

    "return a 401 when an API_KEY header is NOT present" in {
      val result: Future[Result] = fakeController.action.apply(fakeRequest)

      status(result) mustBe 401

    }

  }

}
