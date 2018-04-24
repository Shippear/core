package common

import com.google.inject.Inject
import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter

class Filters @Inject()(loggingFilter: LoggingFilter) extends HttpFilters {
  override def filters: Seq[EssentialFilter] = Seq(loggingFilter)
}
