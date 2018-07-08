package repository.price

import com.google.inject.Inject
import dao.util.ShippearDAO
import model.internal.price.{SizeMultiplier, WeightMultiplier}

import scala.concurrent.ExecutionContext

class WeightMultiplierRepository @Inject()(implicit ec: ExecutionContext) extends MultiplierRepository[WeightMultiplier]{
  override def collectionName: String = "weight-multiplier"

  override def dao: ShippearDAO[WeightMultiplier] = daoFactory[WeightMultiplier](collectionName)
}