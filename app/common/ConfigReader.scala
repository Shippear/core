package common

import java.nio.file.{Files, Paths}
import java.time.Duration
import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigFactory}
import org.joda.time.DateTime
import play.api.Play

import scala.concurrent.duration.FiniteDuration
import scala.util.{Success, Try}

/**
  * Configuration reader that depends on the environments.
  */
trait ConfigReader {
  protected def envConfiguration = ConfigReader.eConfiguration

  protected def stackConfig(configPath: String, inPath: Seq[String] = List.empty[String]): Config =
    inPath.inits
      .map(_.:+(configPath).mkString("."))
      .map(path => Try(envConfiguration.getConfig(path)))
      .reduceRight({
        case (Success(c1), Success(c2)) =>
          Success(c1.withFallback(c2))
        case (try1@Success(_), _) =>
          try1
        case (_, try2) =>
          try2
      }: (Try[Config], Try[Config]) => Try[Config]).get


  protected def stackConfig(configPath: String, inPath: String*)(implicit i1: DummyImplicit): Config =
    stackConfig(configPath, inPath)

  implicit def javaDurationToFiniteDuration(javaDuration: Duration): FiniteDuration = FiniteDuration(javaDuration.toNanos, TimeUnit.NANOSECONDS)

  implicit class RichConfig(config: Config) {
    def isProd = {
      val env = config.getString("environment")
      env == "prod"
    }

    def getFiniteDuration(valueName: String): FiniteDuration =
      config.getDuration(valueName)
  }

}

object ConfigReader extends Logging {

  import scala.collection.JavaConverters._
  import scala.collection.immutable._


  private val EnvironmentKey = "environment"
  private val OverrideKey = "override"
  private val OverrideJvmOpt = "overrideFileDir"
  private val OverrideFileName = "application-override.conf"

  @volatile protected[common] var eConfiguration = initConfiguration()

  private lazy val defaultConfig: Config = ConfigFactory.load()

  private lazy val environment: String = defaultConfig.getString(EnvironmentKey)

  private def initConfiguration(): Config = reloadConfiguration()

  private[this] var lastReload: DateTime = DateTime.now()

  /**
    * Reloads the application's configuration.
    *
    * @return The last time that was successfully updated.
    */
  def reloadConfig(overridingConfigMap: Map[String, _ <: Any] = Map()): Try[DateTime] = synchronized {
    Try {
      info("Reloading the configuration...")
      val olderReload = lastReload

      ConfigFactory.invalidateCaches()
      ConfigReader.eConfiguration = ConfigReader.reloadConfiguration(overridingConfigMap)

      lastReload = DateTime.now()
      olderReload
    }
  }

  private[common] def reloadConfiguration(overridingConfigMap: Map[String, _ <: Any] = Map()): Config = {
    val overrideConf: Config = ConfigFactory.parseMap(overridingConfigMap.asJava)
    val eConf = overrideConf.withFallback(ConfigFactory.load().getConfig(environment).withFallback(defaultConfig))
    Option(System.getProperty(OverrideJvmOpt)).map(loadOverrideConfFile(_, eConf)).getOrElse(eConf)
  }

  private def loadOverrideConfFile(overridePath: String, eConf: Config): Config = {
    val path = Paths.get(overridePath, OverrideFileName)
    if (Files.notExists(path)) {
      eConf
    } else {
      ConfigFactory.parseFile(path.toFile).getConfig(OverrideKey).withFallback(eConf)
    }
  }
}
