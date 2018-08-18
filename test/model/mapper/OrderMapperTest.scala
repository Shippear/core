package model.mapper

import java.util.Date

import common.DateTimeNow._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

class OrderMapperTest extends PlaySpec with MockitoSugar {

  "Order Mapper" should {

    "Calculate and get the await to time" in {

      val dateFrom = rightNowTime
      val dateTo: Date = dateFrom.plusHours(2)

      val result = OrderMapper.calculateOrderTimeout(dateFrom, dateTo)

      result.get mustBe convert2Date(dateFrom.plusMinutes(18))

    }

  }

}
