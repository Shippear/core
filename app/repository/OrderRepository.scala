package repository

import com.google.inject.Inject
import dao.util.ShippearDAO
import model.OrderState.CANCELLED
import model.{Order, User}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class OrderRepository @Inject()(userRepository: UserRepository)(implicit ec: ExecutionContext) extends ShippearRepository[Order] {

  override def collectionName: String = "orders"

  override lazy val dao: ShippearDAO[Order] = daoFactory[Order](collectionName)

  override def create(order: Order) = {
    for {
      result <- super.create(order)
      _ = userRepository.updateUserOrder(order.applicantId, order)
      _ = userRepository.updateUserOrder(order.participantId, order)
    } yield result
  }

  override def update(order: Order) = {
    for {
      result <- super.update(order)
      _ = userRepository.updateUserOrder(order.applicantId, order)
      _ = userRepository.updateUserOrder(order.participantId, order)
      _ = order.carrierId.map(carrierId => userRepository.updateUserOrder(carrierId, order))
    } yield result
  }

  def cancelOrder(id: String): Future[(String, String, String)] = {
    //TODO analizar si hay que verificar el estado del pedido
    for {
      order <- super.findOneById(id)
      updateOrder = order.copy(state = CANCELLED)
      _ = update(updateOrder)
      applicant <- userRepository.findOneById(order.applicantId)
      participant <- userRepository.findOneById(order.participantId)
      someCarrier <- findCarrier(order.carrierId)
      carrier = if (someCarrier.isDefined) someCarrier.get.onesignalId else ""
    } yield (applicant.onesignalId, participant.onesignalId, carrier)

  }

  private def findCarrier(id: Option[String]): Future[Option[User]] = {
   id match {
      case Some(carrierId) =>
        Try(userRepository.findOneById(carrierId)) match {
          case Success(result) => result.map(Some(_))
          case Failure(ex) =>  error(s"Carrier id $carrierId", ex); Future(None)
      }
      case None => Future(None)
    }
  }



}
