package service

import com.google.inject.Inject
import dao.{OrderDAO, UserDAO}
import model.{Order, User}
import model.OrderState._

import scala.concurrent.{ExecutionContext, Future}

class OrderService @Inject()(val dao: OrderDAO, val userDao: UserDAO)(implicit ec: ExecutionContext)
  extends Service[Order]{

  override def create(order: Order) = {
    for{
      result <- super.create(order)
      _ <- updateUser(order.applicantId, order)
      _ <- updateUser(order.participantId, order)
    } yield result
  }

  override def update(order: Order): Future[_] = {
    for {
      result <- super.update(order)
      _ = updateUser(order.applicantId, order)
      _ = updateUser(order.participantId, order)
      _ = order.carrierId.map(carrierId => updateUser(carrierId, order))
      } yield result
  }

  def cancelOrder(id: String): Future[(String, String, String)] = {
    //TODO analizar si hay que verificar el estado del pedido
    val a = for {
      order <- dao.findOneById(id)
      updateOrder = updateOrderState(order, CANCELLED)
      _ = update(updateOrder.get)
      partipantOneSignalId <- userDao.findOneById(order.get.participantId)
      applicantOneSignalId <- userDao.findOneById(order.get.applicantId)
      carrierOneSignalId <- userDao.findOneById(order.get.carrierId.get)

    } yield (applicantOneSignalId,partipantOneSignalId,carrierOneSignalId)

    a.flatten
  }


  private def updateUser(userId: String, order: Order) = {
    for {
      user <- userDao.findOneById(userId)
      newUser <- updateUserOrder(user, order)
      _ = userDao.replaceOne(newUser.get)
    } yield newUser
  }

  private def updateUserOrder(user: Option[User], newOrder: Order): Future[Option[User]] =
    Future(user.map{
      u => u.copy(orders =
        Some(replace(u.orders.getOrElse(Seq.empty), newOrder, newOrder._id.equals)))
      }
    )

  private def updateOrderState(order: Option[Order], newState: OrderState) =
    order.map{ o => o.copy(state = newState) }


}