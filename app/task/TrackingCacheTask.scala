package task
import com.google.inject.Inject
import common.ConfigReader
import repository.CacheRepository

import scala.concurrent.duration.FiniteDuration

class TrackingCacheTask @Inject()(val taskManager: TaskManager, cacheRepository: CacheRepository) extends RepetitveAsyncTask with ConfigReader {

  lazy val config = envConfiguration.getConfig("cache")

  lazy val initialDelay: FiniteDuration = config.getFiniteDuration("initial-delay")

  lazy val interval: FiniteDuration = config.getFiniteDuration("update-interval")

  override protected def doAsync(): Unit = updateCache

  def updateCache = {
    val locationsCache = cacheRepository.cache.clone()

    locationsCache.foreach{ case (_, cacheGeolocation) => cacheRepository.dao.upsertOne(cacheGeolocation) }
  }
}
