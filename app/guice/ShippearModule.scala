package guice

import controller._
import dao.ShippearDBContext
import dao.util.ShippearDAOFactory
import net.codingwell.scalaguice.ScalaModule
import onesignal.OneSignalClient
import repository.{CacheRepository, OrderRepository, UserRepository}
import service.{CacheService, OrderService, UserService}
import task.{TaskManager, TrackingCacheTask}
import qrcodegenerator.QrCodeGenerator
import repository.{OrderRepository, UserRepository}
import service.{OrderService, UserService}

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
    bind[QrCodeGenerator].asEagerSingleton()

    //Tracking cache
    bind[CacheController].asEagerSingleton()
    bind[CacheService].asEagerSingleton()
    bind[CacheRepository].asEagerSingleton()

    //Tasks
    bind[TaskManager].asEagerSingleton()
    bind[TrackingCacheTask].asEagerSingleton()

    //Email http client
    bind[OneSignalClient].asEagerSingleton()

  }
}
