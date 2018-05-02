package guice

import controller.{FrontendController, HomeController, UserController}
import dao.UserDAO
import net.codingwell.scalaguice.ScalaModule
import service.UserService

class ShippearModule extends ScalaModule {
  override def configure(): Unit = {
    bind[HomeController].asEagerSingleton()
    bind[FrontendController].asEagerSingleton()

    //User
    bind[UserController].asEagerSingleton()
    bind[UserService].asEagerSingleton()
    bind[UserDAO].asEagerSingleton()
  }
}
