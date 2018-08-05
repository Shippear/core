package dao

import java.util.concurrent.TimeUnit

import com.google.inject.Inject
import common.ConfigReader
import model.internal._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.bson.codecs.{DEFAULT_CODEC_REGISTRY, Macros}
import org.mongodb.scala.{MongoClient, MongoDatabase, ReadPreference, ScalaWriteConcern, WriteConcern}
import play.api.inject.ApplicationLifecycle

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
  private val userDataOrder = Macros.createCodecProvider[UserDataOrder]()
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
      userDataOrder,
      user,
    ),
    DEFAULT_CODEC_REGISTRY
  )

  def database(name: String): MongoDatabase =
    client.getDatabase(name)
      .withWriteConcern(WriteConcern.ACKNOWLEDGED.withWTimeout(1000, TimeUnit.SECONDS))
      .withReadPreference(ReadPreference.primary())
      .withCodecRegistry(codecRegistry)

}
