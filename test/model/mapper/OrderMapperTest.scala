package model.mapper

import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import com.github.nscala_time.time.Imports.DateTime

class OrderMapperTest extends PlaySpec with MockitoSugar {

  "Order Mapper" should {

    "Calculate and get the await to time" in {

      val dateFrom = DateTime.now()
      val dateTo = dateFrom.plusHours(2)

      val result = OrderMapper.calculateAwait(dateFrom.toDate, dateTo.toDate)

      result.get mustBe dateFrom.plusMinutes(18).toDate

    }

  }

}
