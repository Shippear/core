package common

import java.util.Date

import com.github.nscala_time.time.Imports.DateTime
import org.joda.time.DateTimeZone

object DateTimeNow {

  val zone = DateTimeZone.forID("America/Argentina/Buenos_Aires")

  implicit def convert2Date(dateTime: DateTime): Date = dateTime.toLocalDateTime.toDate

  def rightNowTime: DateTime = DateTime.now.withZone(zone)

  def fromDate(date: Date): DateTime = new DateTime(date)

}
