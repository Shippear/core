package guice

import ai.snips.bsonmacros.DatabaseContext
import controller._
import dao.util.ShippearDAOFactory
import dao.ShippearDBContext
import model.User
import net.codingwell.scalaguice.ScalaModule
import repository.{CacheRepository, OrderRepository, UserRepository}
import service.{CacheService, OrderService, UserService}
import task.{TaskManager, TrackingCacheTask}

class ShippearModule extends ScalaModule {
  override def configure(): Unit = {

    bind[HomeController].asEagerSingleton()
    bind[FrontendController].asEagerSingleton()

    //Context for play to parse objects to json
    bind[ShippearDBContext].asEagerSingleton()

    //DAO Factory
    bind[ShippearDAOFactory].asEagerSingleton()

    //User
    bind[UserController].asEagerSingleton()
    bind[UserService].asEagerSingleton()
    bind[UserRepository].asEagerSingleton()

    //Order
    bind[OrderController].asEagerSingleton()
    bind[OrderService].asEagerSingleton()
    bind[OrderRepository].asEagerSingleton()

    //Tracking cache
    bind[CacheController].asEagerSingleton()
    bind[CacheService].asEagerSingleton()
    bind[CacheRepository].asEagerSingleton()

    //Tasks
    bind[TaskManager].asEagerSingleton()
    bind[TrackingCacheTask].asEagerSingleton()

  }

}
