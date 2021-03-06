package task

import java.util.concurrent.atomic.AtomicBoolean

import com.google.inject.Inject
import com.typesafe.config.Config
import common.{ConfigReader, Logging}
import model.internal.Order
import common.DateTimeNow._
import model.internal.OrderState._
import notification.pushnotification.PushNotificationClient
import repository.OrderRepository
import service.OrderService
import notification.common.EventType._
import notification.email.EmailClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

class CancelOrdersTask @Inject()(val taskManager: TaskManager,
                                 orderRepository : OrderRepository,
                                 orderService : OrderService,
                                 pushNotificationClient: PushNotificationClient,
                                 emailClient: EmailClient)
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
          val newOrder = orderToSave.copy(state = CANCELLED, finalizedDate = Some(rightNowTime))
          orderRepository.update(newOrder)
          pushNotificationClient.sendFlowMulticastNotification(newOrder, ORDER_CANCELED)
          emailClient.createEmail(ORDER_CANCELED, newOrder)
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
