package repository.price

import dao.util.ShippearDAO
import model.internal.price.Multiplier
import repository.ShippearRepository

import scala.concurrent.{ExecutionContext, Future}

trait MultiplierRepository[T <: Multiplier] extends ShippearRepository[T] {

  private val Multiplier = "multiplier"
  def multiplier(value: String)(implicit ec: ExecutionContext): Future[Double] = {
    dao.findOne(Map(Multiplier -> value)).map{
      result => result.multiplier
    }
  }

}
