package dao.embbebedmongo

import common.Logging
import de.flapdoodle.embed.mongo.config.{IMongodConfig, MongodConfigBuilder, Net, RuntimeConfigBuilder}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{Command, MongodExecutable, MongodStarter}
import de.flapdoodle.embed.process.config.io.ProcessOutput
import de.flapdoodle.embed.process.runtime.Network
import org.slf4j.Logger
import play.api.Logger

trait Connection extends Logging{



  //Override this method to personalize testing port
  def embedConnectionPort: Int = { 12345 }

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



}