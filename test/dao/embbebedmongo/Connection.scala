package dao.embbebedmongo

import de.flapdoodle.embed.mongo.config.{IMongodConfig, MongodConfigBuilder, Net}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{MongodExecutable, MongodStarter}
import de.flapdoodle.embed.process.runtime.Network

trait Connection {

  //Override this method to personalize testing port
  def embedConnectionPort: Int = { 12345 }

  //Override this method to personalize MongoDB version
  def embedMongoDBVersion: Version.Main = { Version.Main.PRODUCTION }

  lazy val network = new Net(embedConnectionPort, Network.localhostIsIPv6)

  lazy val mongodConfig: IMongodConfig = new MongodConfigBuilder()
    .version(embedMongoDBVersion)
    .net(network)
    .build

  lazy val runtime: MongodStarter = MongodStarter.getDefaultInstance

  lazy val mongodExecutable: MongodExecutable = runtime.prepare(mongodConfig)



}