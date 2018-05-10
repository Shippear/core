package guice

import controller.{FrontendController, HomeController, OrderController, UserController}
import dao.{ShippearDBContext, OrderDAO, UserDAO}
import net.codingwell.scalaguice.ScalaModule
import service.{OrderService, UserService}

class ShippearModule extends ScalaModule {
  override def configure(): Unit = {
    bind[HomeController].asEagerSingleton()
    bind[FrontendController].asEagerSingleton()

    //User
    bind[UserController].asEagerSingleton()
    bind[UserService].asEagerSingleton()
    bind[UserDAO].asEagerSingleton()

    //Order
    bind[OrderController].asEagerSingleton()
    bind[OrderService].asEagerSingleton()
    bind[OrderDAO].asEagerSingleton()


    //Context for play to parse objects to json
    bind[ShippearDBContext]
  }
}
