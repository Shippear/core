package service

import com.google.inject.Inject
import model.CacheGeolocation
import repository.CacheRepository

import scala.concurrent.ExecutionContext

class CacheService @Inject()(val repository: CacheRepository)(implicit ec: ExecutionContext) extends Service[CacheGeolocation]{

  def updateLocation(geolocation: CacheGeolocation) = repository.updateGeolocation(geolocation)

  def geolocation(idUser: String) = repository.geolocation(idUser)

}
