package common

import com.github.nscala_time.time.Imports.DateTime
import org.joda.time.DateTimeZone

object DateTimeNow {

  val now = DateTime.now(DateTimeZone.forID("America/Argentina/Buenos_Aires")).minusHours(3)

}
