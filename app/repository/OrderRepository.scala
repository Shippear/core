package repository

import com.google.inject.Inject
import dao.util.ShippearDAO
import model.internal.OrderState.{CANCELLED, PENDING_PICKUP}
import model.internal.UserType._
import model.internal.{Order, User}
import service.Exception.NotFoundException

import scala.concurrent.{ExecutionContext, Future}

class OrderRepository @Inject()(userRepository: UserRepository)(implicit ec: ExecutionContext) extends ShippearRepository[Order] {
  def assignCarrier(orderId : String, carrierId : String, qrCode : Array[Byte]): Future[Unit] ={
    for{
      order <- super.findOneById(orderId)
      newOrder = order.copy(carrierId = Some(carrierId), qrCode = Some(qrCode), state = PENDING_PICKUP)
      _ <- update(newOrder)
      _ <- userRepository.updateUserOrder(newOrder.applicantId, newOrder)
      _ <- userRepository.updateUserOrder(newOrder.participantId, newOrder)
      _ <- userRepository.updateUserOrder(carrierId, newOrder)
    } yield newOrder
  }

  def validateQrCode(orderId : String, userId : String, userType : UserType): Future[Any] ={
    val order = findOneById(orderId)
    order.map{ order =>
        userType match{
        case APPLICANT => order.applicantId.equals(userId)
        case PARTICIPANT => order.participantId.equals(userId)
        case CARRIER =>  order.carrierId.getOrElse().equals(userId)
      }
    }
  }

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

  def cancelOrder(id: String): Future[(String, String, Option[String])] = {
    //TODO analizar si hay que verificar el estado del pedido
    for {
      order <- super.findOneById(id)
      updateOrder = order.copy(state = CANCELLED)
      _ = update(updateOrder)
      applicant <- userRepository.findOneById(order.applicantId)
      participant <- userRepository.findOneById(order.participantId)
      someCarrier <- findCarrier(order.carrierId)
    } yield (applicant.onesignalId, participant.onesignalId, someCarrier.map(_.onesignalId))

  }

  private def findCarrier(id: Option[String]): Future[Option[User]] = {
   id match {
      case Some(carrierId) => userRepository.findOneById(carrierId).map(Some(_)).recover {case _: NotFoundException => None}
      case None => Future.successful(None)
    }
  }



}
