package repository.price

import com.google.inject.Inject
import dao.util.ShippearDAO
import model.internal.price.SizeMultiplier

import scala.concurrent.ExecutionContext

class SizeMultiplierRepository @Inject()(implicit ec: ExecutionContext) extends MultiplierRepository[SizeMultiplier]{
  override def collectionName: String = "size-multiplier"

  override def dao: ShippearDAO[SizeMultiplier] = daoFactory[SizeMultiplier](collectionName)
}
