package repository

import com.google.inject.Inject
import dao.util.ShippearDAO
import model.internal.OrderState.{CANCELLED, PENDING_PICKUP}
import model.internal.UserType._
import model.internal.{Order, User}
import onesignal.OneSignalClient
import service.Exception.NotFoundException

import scala.concurrent.{ExecutionContext, Future}

class OrderRepository @Inject()(userRepository: UserRepository)(implicit ec: ExecutionContext) extends ShippearRepository[Order] {

  override def collectionName: String = "orders"

  override lazy val dao: ShippearDAO[Order] = daoFactory[Order](collectionName)

  override def create(order: Order) = {
    for {
      result <- super.create(order)
      _ <- userRepository.updateUserOrder(order.applicantId, order)
      _ <- userRepository.updateUserOrder(order.participantId, order)
    } yield result

  }

  override def update(order: Order) = {
    for {
      result <- super.update(order)
      _ <- userRepository.updateUserOrder(order.applicantId, order)
      _ <- userRepository.updateUserOrder(order.participantId, order)
      _ <- updateCarrier(order)
    } yield result
  }

  def cancelOrder(id: String): Future[(String, String, Option[String])] = {
    //TODO analizar si hay que verificar el estado del pedido
    for {
      order <- super.findOneById(id)
      updateOrder = order.copy(state = CANCELLED)
      _ <- update(updateOrder)
      applicant <- userRepository.findOneById(order.applicantId)
      participant <- userRepository.findOneById(order.participantId)
      someCarrier <- findCarrier(order.carrierId)
    } yield (applicant.onesignalId, participant.onesignalId, someCarrier.map(_.onesignalId))

  }

  def assignCarrier(orderId: String, carrier: User, qrCode: Array[Byte]): Future[Order] ={
    for{
      order <- super.findOneById(orderId)
      newOrder = order.copy(carrierId = Some(carrier._id), qrCode = Some(qrCode), state = PENDING_PICKUP)
      _ <- update(newOrder)
    } yield newOrder
  }

  def validateQrCode(orderId: String, userId: String, userType: UserType): Future[Boolean] =
    findOneById(orderId).map{ order =>
      userType match{
        case APPLICANT => order.applicantId.equals(userId)
        case PARTICIPANT => order.participantId.equals(userId)
        case CARRIER =>  order.carrierId.getOrElse(throw NotFoundException("Carrier not found")).equals(userId)
      }
    }

  private def findCarrier(id: Option[String]): Future[Option[User]] = {
   id match {
      case Some(carrierId) => userRepository.findOneById(carrierId).map(Some(_)).recover {case _: NotFoundException => None}
      case None => Future.successful(None)
    }
  }

  private def updateCarrier(order: Order): Future[_] = {
    order.carrierId match {
      case Some(id) => userRepository.updateUserOrder(id, order)
      case _ => Future.successful(Unit)
    }
  }

}
