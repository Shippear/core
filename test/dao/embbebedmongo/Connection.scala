package dao.embbebedmongo

import common.{ConfigReader, Logging}
import database.MongoConfiguration
import de.flapdoodle.embed.mongo.config.{IMongodConfig, MongodConfigBuilder, Net, RuntimeConfigBuilder}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{Command, MongodExecutable, MongodStarter}
import de.flapdoodle.embed.process.runtime.Network
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.mongodb.scala.MongoClient

trait Connection extends Logging with ConfigReader {

  protected val config: MongoConfiguration = envConfiguration.as[MongoConfiguration]("mongodb")

  //Override this method to personalize testing port
  def embedConnectionPort: Int = config.port.getOrElse(12345)

  //Override this method to personalize MongoDB version
  def embedMongoDBVersion: Version.Main = { Version.Main.PRODUCTION }

  lazy val network = new Net(embedConnectionPort, Network.localhostIsIPv6)

  lazy val mongodConfig: IMongodConfig = new MongodConfigBuilder()
    .version(embedMongoDBVersion)
    .net(network)
    .build

  lazy val runtime: MongodStarter = {
    val runtimeConfig = new RuntimeConfigBuilder()
      .defaultsWithLogger(Command.MongoD, LOGGER.logger)
      .build()

    MongodStarter.getInstance(runtimeConfig)
  }

  lazy val mongodExecutable: MongodExecutable = runtime.prepare(mongodConfig)

  lazy val mongo = MongoClient(config.uri)

}