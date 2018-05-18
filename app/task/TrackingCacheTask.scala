package task
import java.util.concurrent.atomic.AtomicBoolean

import com.google.inject.Inject
import common.{ConfigReader, Logging}
import repository.CacheRepository

import scala.concurrent.duration.FiniteDuration

class TrackingCacheTask @Inject()(val taskManager: TaskManager, cacheRepository: CacheRepository)
  extends RepetitveAsyncTask with ConfigReader with Logging {

  lazy val config = envConfiguration.getConfig("cache")

  lazy val initialDelay: FiniteDuration = config.getFiniteDuration("initial-delay")

  lazy val interval: FiniteDuration = config.getFiniteDuration("update-interval")

  private val activated: AtomicBoolean = new AtomicBoolean(config.getBoolean("activated"))

  def isActivated = activated.get()

  def setActivated_=(value: Boolean) = activated.set(value)

  override protected def doAsync(): Unit = updateCache

  def updateCache = {
    if(isActivated) {
      val locationsCache = cacheRepository.cache.clone()
      info("Saving locations cache to DB...")
      locationsCache.foreach { case (_, cacheGeolocation) => cacheRepository.dao.upsertOne(cacheGeolocation) }
    }
  }
}
