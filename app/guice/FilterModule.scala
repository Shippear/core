package guice

import common.{Filters, LoggingFilter}
import net.codingwell.scalaguice.ScalaModule

class FilterModule extends ScalaModule {

  override def configure(): Unit = {
    bind[LoggingFilter].asEagerSingleton()
    bind[Filters].asEagerSingleton()

  }

}
