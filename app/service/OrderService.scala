package service

import com.google.inject.Inject
import model.internal.OrderState.OrderState
import model.internal._
import onesignal.{EmailType, OneSignalClient}
import qrcodegenerator.QrCodeGenerator
import qrcodegenerator.QrCodeGenerator._
import repository.{OrderRepository, UserRepository}
import service.Exception.ShippearException

import scala.concurrent.{ExecutionContext, Future}

class OrderService @Inject()(val repository: OrderRepository, mailClient: OneSignalClient,
                             qrCodeGenerator: QrCodeGenerator, userRepository: UserRepository)(implicit ec: ExecutionContext)
  extends Service[Order]{

  def createOrder(newOrder: Order) = {
    for {
      applicant <- userRepository.findOneById(newOrder.applicantId)
      participant <- userRepository.findOneById(newOrder.participantId)
      result <- repository.create(newOrder)
      list = List(applicant.onesignalId, participant.onesignalId)
      _ = mailClient.sendEmail(list, EmailType.ORDER_CREATED)
    } yield result
  }

  def cancelOrder(id: String): Future[(String, String, Option[String])] =
    for {
      (applicantOSId, participantOSId, carrierOSId) <- repository.cancelOrder(id)
      list = List(applicantOSId, participantOSId) ++ carrierOSId
      _ = mailClient.sendEmail(list, EmailType.ORDER_CANCELED)
    } yield (applicantOSId, participantOSId, carrierOSId)


  def assignCarrier(content: AssignCarrier) =
    for {
      carrier <- userRepository.findOneById(content.carrierId)
      _ = validateCarrier(carrier)
      order <- repository.findOneById(content.orderId)
      _ = validateOrderState(order.state, OrderState.PENDING_CARRIER)
      newOrder <- repository.assignCarrier(order, carrier, qrCodeGenerator.generateQrImage(content.orderId))
      list = List(newOrder.applicantId, newOrder.participantId) ++ newOrder.carrierId
      _ = mailClient.sendEmail(list, EmailType.ORDER_WITH_CARRIER)
    } yield newOrder


  def validateQrCode(content: OrderToValidate): Future[Boolean] =
    repository.validateQrCode(content.orderId, content.userId, content.userType)

  def validateCarrier(carrier: User): Unit =
    carrier.orders match {
      case Some(list) =>
        val assigned = list.filter{
          order =>
            val carrierId = order.carrierId.getOrElse("")
            val orderState: OrderState = order.state

            carrierId.equals(carrier._id) &&
              (orderState.equals(OrderState.ON_TRAVEL) || orderState.equals(OrderState.PENDING_PICKUP))

        }

        if(assigned.size == 3) throw ShippearException(s"Carrier with id ${carrier._id} already has 3 orders assigned")
      case _ => ()
    }

  def validateOrderState(orderState: OrderState, expectingState: OrderState) = {
    if(!orderState.equals(expectingState)) throw ShippearException(s"Order must be in state $expectingState, not in $orderState")
  }
}