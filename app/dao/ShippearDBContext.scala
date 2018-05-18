package dao

import ai.snips.bsonmacros.{CodecGen, DatabaseContext, DynamicCodecRegistry}
import com.google.inject.Inject
import common.ConfigReader
import model._
import org.mongodb.scala.{MongoClient, MongoDatabase, ReadPreference, WriteConcern}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}

class ShippearDBContext @Inject()(val applicationLifecycle: ApplicationLifecycle)(implicit ec: ExecutionContext)
  extends ConfigReader{

  lazy val mongoConf: String = envConfiguration.getString("mongodb.uri")
  lazy val client = MongoClient(mongoConf)
  lazy val codecRegistry = new DynamicCodecRegistry

  applicationLifecycle.addStopHook { () =>
    Future.successful(client.close())
  }

  def database(name: String): MongoDatabase =
    client.getDatabase(name).withWriteConcern(WriteConcern.ACKNOWLEDGED).withReadPreference(ReadPreference.primary()).withCodecRegistry(codecRegistry)

  def ping(): Future[Unit] =
    client.listDatabaseNames().toFuture.map(_ => ())

  CodecGen[Transport](codecRegistry)
  CodecGen[Geolocation](codecRegistry)
  CodecGen[CacheGeolocation](codecRegistry)
  CodecGen[PaymentMethod](codecRegistry)
  CodecGen[ContactInfo](codecRegistry)
  CodecGen[Address](codecRegistry)
  CodecGen[Route](codecRegistry)
  CodecGen[Address](codecRegistry)
  CodecGen[Order](codecRegistry)
  CodecGen[User](codecRegistry)


}
