package service

import com.google.inject.Inject
import model.{Order, User}
import model.OrderState._
import repository.{OrderRepository, UserRepository}
import service.Exception.NotFoundException

import scala.concurrent.{ExecutionContext, Future}

class OrderService @Inject()(val repository: OrderRepository)(implicit ec: ExecutionContext)
  extends Service[Order]{

  def cancelOrder(id: String): Future[(String, String, String)] =
   repository.cancelOrder(id)



}