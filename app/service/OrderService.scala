package service

import com.google.inject.Inject
import model.internal.OrderState.OrderState
import model.internal._
import model.mapper.OrderMapper
import model.request.OrderCreation
import onesignal.{EmailType, OneSignalClient}
import qrcodegenerator.QrCodeGenerator
import qrcodegenerator.QrCodeGenerator._
import repository.{OrderRepository, UserRepository}
import service.Exception.ShippearException

import scala.concurrent.{ExecutionContext, Future}

class OrderService @Inject()(val repository: OrderRepository, mailClient: OneSignalClient,
                             qrCodeGenerator: QrCodeGenerator, userRepository: UserRepository)
                            (implicit ec: ExecutionContext) extends Service[Order]{

  def createOrder(newOrder: OrderCreation) = {
    for {
      applicant <- userRepository.findOneById(newOrder.applicantId)
      participant <- userRepository.findOneById(newOrder.participantId)
      orderToSave = OrderMapper.orderCreationToOrder(newOrder, applicant, participant)
      _ <- repository.create(orderToSave)
      _ = mailClient.sendEmail(List(applicant.onesignalId, participant.onesignalId), EmailType.ORDER_CREATED)
    } yield orderToSave
  }


  def cancelOrder(id: String): Future[(String, String, Option[String])] =
    for {
      (applicantOSId, participantOSId, carrierOSId) <- repository.cancelOrder(id)
      _ = mailClient.sendEmail(List(applicantOSId, participantOSId) ++ carrierOSId, EmailType.ORDER_CANCELED)
    } yield (applicantOSId, participantOSId, carrierOSId)


  def confirmParticipant(orderId: String): Future[Order] = {
    for {
      order <- repository.findOneById(orderId)
      _ = validateOrderState(order.state, OrderState.PENDING_PARTICIPANT)
      _ <- repository.update(order.copy(state = OrderState.PENDING_CARRIER))
    } yield order
  }

  def assignCarrier(content: AssignCarrier) =
    for {
      carrier <- userRepository.findOneById(content.carrierId)
      _ = validateCarrier(carrier)
      order <- repository.findOneById(content.orderId)
      _ = validateOrderState(order.state, OrderState.PENDING_CARRIER)
      newOrder <- repository.assignCarrier(order, carrier, qrCodeGenerator.generateQrImage(content.orderId))
      _ = mailClient.sendEmail(List(newOrder.applicant.oneSignalId, newOrder.participant.oneSignalId, carrier.onesignalId), EmailType.ORDER_WITH_CARRIER)
    } yield newOrder


  def validateQrCode(content: OrderToValidate): Future[Boolean] =
    repository.validateQrCode(content.orderId, content.userId, content.userType)

  def validateCarrier(carrier: User): Unit =
    carrier.orders match {
      case Some(list) =>
        val assigned = list.filter {
          order =>
            if(order.carrier.isDefined) {
              val carrierId = order.carrier.get.id
              val orderState: OrderState = order.state

              carrierId.equals(carrier._id) &&
                (orderState.equals(OrderState.ON_TRAVEL) || orderState.equals(OrderState.PENDING_PICKUP))
            }
            else
              false

        }

        if(assigned.size == 3) throw ShippearException(s"Carrier with id ${carrier._id} already has 3 orders assigned")
      case _ => ()
    }

  def validateOrderState(orderState: OrderState, expectingState: OrderState) = {
    if(!orderState.equals(expectingState)) throw ShippearException(s"Order must be in state $expectingState, not in $orderState")
  }
}