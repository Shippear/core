package task

import java.util.concurrent.atomic.AtomicBoolean

import com.google.inject.Inject
import com.typesafe.config.Config
import common.{ConfigReader, Logging}
import model.internal.OrderState
import repository.OrderRepository

import scala.concurrent.ExecutionContext.Implicits.global
import common.DateTimeNow
import service.OrderService

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class CancelOrdersTask @Inject()(val taskManager: TaskManager, orderRepository : OrderRepository, orderService : OrderService)
  extends RepetitveAsyncTask with ConfigReader with Logging{

  lazy val config: Config = envConfiguration.getConfig("timeOut")

  lazy val initialDelay: FiniteDuration = config.getFiniteDuration("initial-delay")

  lazy val interval: FiniteDuration = config.getFiniteDuration("update-interval")

  private val activated: AtomicBoolean = new AtomicBoolean(config.getBoolean("activated"))

  def isActivated: Boolean = activated.get()

  def setActivated_=(value: Boolean): Unit = activated.set(value)

  override protected def doAsync(): Unit = updateOrderState()

  def updateOrderState(): Unit = {
    if(isActivated) {
      info("Find orders to update respect time out...")
      val orders = orderService.all
      orders.map { ordersToSave =>
        for {order <- ordersToSave
             awaitTo = order.awaitTo
             _ = awaitTo match {
               case Some(awaiTo) => if (order.state.equals(OrderState.PENDING_CARRIER.toString) && DateTimeNow.now.toDate.after(awaiTo)) {
                 orderRepository.cancelOrder(order._id)
               }
               case None => Future.successful(Unit)
             }

        } yield order

      }

    }
  }
}
