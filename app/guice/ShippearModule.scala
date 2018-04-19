package guice

import controllers.{FrontendController, HomeController, UserController}
import net.codingwell.scalaguice.ScalaModule

class ShippearModule extends ScalaModule {
  override def configure(): Unit = {
    bind[HomeController].asEagerSingleton()
    bind[FrontendController].asEagerSingleton()
    bind[UserController].asEagerSingleton()
  }
}
