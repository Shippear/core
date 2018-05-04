package controller

import org.scalatest.TestSuite
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import org.specs2.mutable.Specification
import play.api.libs.json.{JsObject, JsValue}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}



class HomeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  private def parseContent(content: JsValue) =
    content.asInstanceOf[JsObject].value("value").asInstanceOf[JsObject].value("content").toString

  "HomeController GET" should {

    "render the appSummary resource from a new instance of controller" in {

      val controller = new HomeController
      controller.setControllerComponents(stubControllerComponents())
      val home = controller.appSummary().apply(FakeRequest(GET, "/summary"))

      status(home) mustEqual OK
      contentType(home) mustBe Some("application/json")
      val resultJson = contentAsJson(home)
      parseContent(resultJson) mustEqual """{"value":"Scala Play React Seed"}"""

    }


    "render the appSummary resource from the application" in {
      val controller = inject[HomeController]
      val home = controller.appSummary().apply(FakeRequest(GET, "/summary"))

      status(home) mustEqual OK
      contentType(home) mustBe Some("application/json")
      val resultJson = contentAsJson(home)
      parseContent(resultJson) mustEqual """{"value":"Scala Play React Seed"}"""
    }

    "render the appSummary resource from the router" in {
      val request = FakeRequest(GET, "/shippear/summary")
      val home = route(app, request).get

      status(home) mustEqual OK
      contentType(home) mustBe Some("application/json")
      val resultJson = contentAsJson(home)
      parseContent(resultJson) mustEqual """{"value":"Scala Play React Seed"}"""
    }

  }

}
