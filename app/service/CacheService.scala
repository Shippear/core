package service

import com.google.inject.Inject
import model.internal.CacheGeolocation
import repository.CacheRepository
import task.TrackingCacheTask

import scala.concurrent.ExecutionContext

class CacheService @Inject()(val repository: CacheRepository, val cacheTask: TrackingCacheTask)
                            (implicit ec: ExecutionContext) extends Service[CacheGeolocation]{

  def updateLocation(geolocation: CacheGeolocation) = repository.updateGeolocation(geolocation)

  def geolocation(idUser: String) = repository.geolocation(idUser)

  def active(value: Boolean) = {
    cacheTask.setActivated_=(value)
    cacheTask.isActivated
  }

}
