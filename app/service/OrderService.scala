package service

import com.google.inject.Inject
import model.internal.Order
import onesignal.{EmailType, OneSignalClient}
import repository.OrderRepository

import scala.concurrent.{ExecutionContext, Future}

class OrderService @Inject()(val mailClient: OneSignalClient, val repository: OrderRepository)(implicit ec: ExecutionContext)
  extends Service[Order]{

  def cancelOrder(id: String): Future[(String, String, Option[String])] =
   repository.cancelOrder(id)

  def sendEmail(oneSignal: String) = mailClient.sendEmail(List(oneSignal), EmailType.ORDER_ON_WAY)

  def device(userOneSignalId: Option[String]) = mailClient.device(userOneSignalId)

}