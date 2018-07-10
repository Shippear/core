package repository

import com.google.inject.Inject
import dao.util.ShippearDAO
import model.internal.OrderState.{CANCELLED, PENDING_PICKUP}
import model.internal.UserType._
import model.internal.{Order, User, UserDataOrder}
import model.mapper.OrderMapper
import service.Exception.NotFoundException

import scala.concurrent.{ExecutionContext, Future}

class OrderRepository @Inject()(userRepository: UserRepository)(implicit ec: ExecutionContext) extends ShippearRepository[Order] {

  override def collectionName: String = "orders"

  override lazy val dao: ShippearDAO[Order] = daoFactory[Order](collectionName)

  override def create(order: Order) = {
    for {
      result <- super.create(order)
      _ <- userRepository.updateUserOrder(order.applicant.id, order)
      _ <- userRepository.updateUserOrder(order.participant.id, order)
    } yield result

  }

  override def update(order: Order) = {
    for {
      result <- super.update(order)
      _ <- userRepository.updateUserOrder(order.applicant.id, order)
      _ <- userRepository.updateUserOrder(order.participant.id, order)
      _ <- updateCarrier(order)
    } yield result
  }

  def cancelOrder(id: String): Future[(String, String, Option[String])] = {
    //TODO analizar si hay que verificar el estado del pedido
    for {
      order <- super.findOneById(id)
      updateOrder = order.copy(state = CANCELLED)
      _ <- update(updateOrder)
      applicant <- userRepository.findOneById(order.applicant.id)
      participant <- userRepository.findOneById(order.participant.id)
      someCarrier <- findCarrier(order.carrier)
    } yield (applicant.onesignalId, participant.onesignalId, someCarrier.map(_.onesignalId))

  }

  def assignCarrier(order: Order, carrier: User, qrCode: Array[Byte]): Future[Order] = {
    val carrierData = OrderMapper.extractUserData(carrier)
    val newOrder = order.copy(carrier = Some(carrierData), qrCode = Some(qrCode), state = PENDING_PICKUP)
    for{
      _ <- update(newOrder)
    } yield newOrder
  }

  def validateQrCode(orderId: String, userId: String, userType: UserType): Future[Boolean] =
    findOneById(orderId).map{ order =>
      userType match{
        case APPLICANT => order.applicant.id.equals(userId)
        case PARTICIPANT => order.participant.id.equals(userId)
        case CARRIER =>  order.carrier.getOrElse(throw NotFoundException("Carrier not found")).id.equals(userId)
      }
    }

  private def findCarrier(carrier: Option[UserDataOrder]): Future[Option[User]] = {
   carrier match {
      case Some(data) => userRepository.findOneById(data.id).map(Some(_)).recover {case _: NotFoundException => None}
      case None => Future.successful(None)
    }
  }

  private def updateCarrier(order: Order): Future[_] = {
    order.carrier match {
      case Some(carrier) => userRepository.updateUserOrder(carrier.id, order)
      case _ => Future.successful(Unit)
    }
  }

}
