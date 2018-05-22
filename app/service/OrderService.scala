package service

import com.google.inject.Inject
import model.internal.Order
import onesignal.OneSignalClient
import repository.OrderRepository

import scala.concurrent.{ExecutionContext, Future}

class OrderService @Inject()(val mailClient: OneSignalClient, val repository: OrderRepository)(implicit ec: ExecutionContext)
  extends Service[Order]{

  def cancelOrder(id: String): Future[(String, String, Option[String])] =
   repository.cancelOrder(id)

  def test(oneSignal: String) = Future(mailClient.createEmail(List(oneSignal)))

}