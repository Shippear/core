package service

import com.google.inject.Inject
import model.internal.Order
import repository.OrderRepository

import scala.concurrent.{ExecutionContext, Future}

class OrderService @Inject()(val repository: OrderRepository)(implicit ec: ExecutionContext)
  extends Service[Order]{

  def cancelOrder(id: String): Future[(String, String, Option[String])] =
   repository.cancelOrder(id)



}