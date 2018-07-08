package repository.price

import model.internal.price.Multiplier
import repository.ShippearRepository

import scala.concurrent.{ExecutionContext, Future}

trait MultiplierRepository[T <: Multiplier] extends ShippearRepository[T] {

  protected def Key: String
  def multiplier(keyValue: String)(implicit ec: ExecutionContext): Future[Double] = {
    dao.findOne(Map(Key -> keyValue)).map{
      result => result.multiplier
    }
  }

}
