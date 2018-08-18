package task

import java.util.concurrent.atomic.AtomicBoolean

import com.google.inject.Inject
import com.typesafe.config.Config
import common.{ConfigReader, Logging}
import model.internal.Order
import common.DateTimeNow._
import model.internal.OrderState._
import repository.OrderRepository
import service.OrderService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

class CancelOrdersTask @Inject()(val taskManager: TaskManager, orderRepository : OrderRepository, orderService : OrderService)
  extends RepetitveAsyncTask with ConfigReader with Logging{

  lazy val config: Config = envConfiguration.getConfig("timeout")

  lazy val initialDelay: FiniteDuration = config.getFiniteDuration("initial-delay")

  lazy val interval: FiniteDuration = config.getFiniteDuration("update-interval")

  private val activated: AtomicBoolean = new AtomicBoolean(config.getBoolean("activated"))

  def isActivated: Boolean = activated.get()

  def setActivated_=(value: Boolean): Unit = activated.set(value)

  override protected def doAsync(): Unit = updateOrderState()

  def updateOrderState(): Unit = {
    if(isActivated) {
      info("Find orders to cancel due respect time out...")
      for {
        orders <- orderService.all
        ordersToCancel = orderToCancel(orders)
      } yield ordersToCancel.foreach {
        orderToSave => {
          info(s"Cancelling order ${orderToSave._id} due time out")
          orderRepository.update(orderToSave.copy(state = CANCELLED, finalizedDate = Some(rightNowTime)))
        }
      }
    }

  }

  private def orderToCancel(orders: Seq[Order]): Seq[Order] =
    orders.filter {
      order => order.timeoutTime match {
          case Some(date) =>
            date.before(rightNowTime) &&
              (order.state.equals(PENDING_PARTICIPANT.toString) ||
                order.state.equals(PENDING_CARRIER.toString))
          case _ => false
        }
    }
}
