package repository

import com.google.inject.Inject
import dao.util.ShippearDAO
import model.CacheGeolocation

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext

class CacheRepository @Inject()(implicit ec: ExecutionContext) extends ShippearRepository[CacheGeolocation]{

  override def collectionName: String = "locations"

  override lazy val dao: ShippearDAO[CacheGeolocation] = daoFactory[CacheGeolocation](collectionName)

  private lazy val cache: TrieMap[String, CacheGeolocation] = {
    val trieMap = TrieMap.empty[String, CacheGeolocation]
    dao.all.map{
      locations => locations.foreach(geo => trieMap.putIfAbsent(geo._id, geo))
    }

    trieMap
  }

  def locations = cache.snapshot()

  def updateGeolocation(geolocation: CacheGeolocation) = {
    cache.put(geolocation._id, geolocation)
  }

  def geolocation(idUser: String) = {
    cache.get(idUser)
  }

}
