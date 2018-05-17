package guice

import ai.snips.bsonmacros.DatabaseContext
import controller.{FrontendController, HomeController, OrderController, UserController}
import dao.util.ShippearDAOFactory
import dao.ShippearDBContext
import model.User
import net.codingwell.scalaguice.ScalaModule
import repository.{OrderRepository, UserRepository}
import service.{OrderService, UserService}

class ShippearModule extends ScalaModule {
  override def configure(): Unit = {
    bind[HomeController].asEagerSingleton()
    bind[FrontendController].asEagerSingleton()

    //Context for play to parse objects to json
    bind[ShippearDBContext].asEagerSingleton()
    bind[ShippearDAOFactory].asEagerSingleton()
    bind[DatabaseContext].asEagerSingleton()

    //User
    bind[UserController].asEagerSingleton()
    bind[UserService].asEagerSingleton()
    bind[UserRepository].asEagerSingleton()

    //Order
    bind[OrderController].asEagerSingleton()
    bind[OrderService].asEagerSingleton()
    bind[OrderRepository].asEagerSingleton()

  }
}
