package dao

import com.google.inject.Inject
import common.ConfigReader
import model.internal._
import org.mongodb.scala.{MongoClient, MongoDatabase, ReadPreference, WriteConcern}
import play.api.inject.ApplicationLifecycle
import org.mongodb.scala.bson.codecs.{DEFAULT_CODEC_REGISTRY, Macros}
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}

import scala.concurrent.{ExecutionContext, Future}

class ShippearDBContext @Inject()(val applicationLifecycle: ApplicationLifecycle)(implicit ec: ExecutionContext)
  extends ConfigReader{

  lazy val mongoConf: String = envConfiguration.getString("mongodb.uri")
  lazy val client = MongoClient(mongoConf)

  applicationLifecycle.addStopHook { () =>
    Future.successful(client.close())
  }

  private val transport = Macros.createCodecProviderIgnoreNone[Transport]()
  private val geolocation = Macros.createCodecProviderIgnoreNone[Geolocation]()
  private val cacheGeolocation = Macros.createCodecProviderIgnoreNone[CacheGeolocation]()
  private val paymentMethods = Macros.createCodecProviderIgnoreNone[PaymentMethod]()
  private val contactInfo = Macros.createCodecProviderIgnoreNone[ContactInfo]()
  private val city = Macros.createCodecProviderIgnoreNone[City]()
  private val address = Macros.createCodecProviderIgnoreNone[Address]()
  private val route = Macros.createCodecProviderIgnoreNone[Route]()
  private val order = Macros.createCodecProviderIgnoreNone[Order]()
  private val user = Macros.createCodecProviderIgnoreNone[User]()

  val codecRegistry = fromRegistries(
    fromProviders(
      transport,
      geolocation,
      cacheGeolocation,
      paymentMethods,
      contactInfo,
      city,
      address,
      route,
      order,
      user,
    ),
    DEFAULT_CODEC_REGISTRY
  )

  def database(name: String): MongoDatabase =
    client.getDatabase(name).withWriteConcern(WriteConcern.ACKNOWLEDGED).withReadPreference(ReadPreference.primary()).withCodecRegistry(codecRegistry)

  def ping(): Future[Unit] =
    client.listDatabaseNames().toFuture.map(_ => ())

}
