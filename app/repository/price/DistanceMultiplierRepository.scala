package repository.price

import com.google.inject.Inject
import dao.util.ShippearDAO
import model.internal.price.DistanceMultiplier

import scala.concurrent.ExecutionContext

class DistanceMultiplierRepository  @Inject()(implicit ec: ExecutionContext) extends MultiplierRepository[DistanceMultiplier]{

  override def Key = "distance"
  override def collectionName: String = "distance-multiplier"
  override def dao: ShippearDAO[DistanceMultiplier] = daoFactory[DistanceMultiplier](collectionName)

  val Scenario = "scenario"

  def multiplierByScenario(scenario: String) = {
    dao.findOne(Map(Scenario -> scenario)).map{result => result.multiplier}
  }

}

object Scenario extends Enumeration {
  type Scenario = Value

  val NORMAL, URGENT = Value

  implicit def toString(scenario: Scenario): String = scenario.toString

  implicit def toScenario(scenario: String): Scenario = withName(scenario.toUpperCase)

}
