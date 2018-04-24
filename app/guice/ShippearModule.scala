package guice

import controller.{FrontendController, HomeController, UserController}
import database.Mongo
import net.codingwell.scalaguice.ScalaModule
import service.UserService

class ShippearModule extends ScalaModule {
  override def configure(): Unit = {
    bind[HomeController].asEagerSingleton()
    bind[FrontendController].asEagerSingleton()
    bind[UserController].asEagerSingleton()

    bind[UserService]

    bind[Mongo].asEagerSingleton()
  }
}
