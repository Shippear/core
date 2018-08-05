package guice

import controller._
import dao.ShippearDBContext
import dao.util.ShippearDAOFactory
import external.GoogleMapsClient
import model.mapper.OrderMapper
import net.codingwell.scalaguice.ScalaModule
import onesignal.OneSignalClient
import qrcodegenerator.QrCodeGenerator
import repository.price.{DistanceMultiplierRepository, SizeMultiplierRepository, WeightMultiplierRepository}
import repository.{CacheRepository, OrderRepository, UserRepository}
import service.{PriceService, _}
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
    bind[QrCodeGenerator].asEagerSingleton()

    //Tracking cache
    bind[CacheController].asEagerSingleton()
    bind[CacheService].asEagerSingleton()
    bind[CacheRepository].asEagerSingleton()

    //Tasks
    bind[TaskManager].asEagerSingleton()
    bind[TrackingCacheTask].asEagerSingleton()

    //Email
    bind[MailController].asEagerSingleton()
    bind[MailService].asEagerSingleton()
    bind[OneSignalClient].asEagerSingleton()

    //RouteMap
    bind[RouteMapController].asEagerSingleton()
    bind[RoutePriceService].asEagerSingleton()
    bind[GoogleMapsClient].asEagerSingleton()

    //Prices
    bind[PriceService].asEagerSingleton()

    //Multipliers for price
    bind[SizeMultiplierRepository].asEagerSingleton()
    bind[WeightMultiplierRepository].asEagerSingleton()
    bind[DistanceMultiplierRepository].asEagerSingleton()

  }
}
