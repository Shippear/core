package service

import com.google.inject.Inject
import common.DateTimeNow
import model.internal.OrderState.OrderState
import model.internal.UserType.{APPLICANT, CARRIER, PARTICIPANT, UserType}
import model.internal._
import model.mapper.OrderMapper
import model.request.OrderCreation
import onesignal.{EmailType, OneSignalClient}
import qrcodegenerator.QrCodeGenerator
import qrcodegenerator.QrCodeGenerator._
import repository.{OrderRepository, UserRepository}
import service.Exception.{NotFoundException, ShippearException}

import scala.concurrent.{ExecutionContext, Future}

class OrderService @Inject()(val repository: OrderRepository, mailClient: OneSignalClient,
                             qrCodeGenerator: QrCodeGenerator, userRepository: UserRepository)
                            (implicit ec: ExecutionContext) extends Service[Order]{

  def createOrder(newOrder: OrderCreation) = {
    validateOrder(newOrder)
      for {
        applicant <- userRepository.findOneById(newOrder.applicantId)
        participant <- userRepository.findOneById(newOrder.participantId)
        orderToSave = OrderMapper.orderCreationToOrder(newOrder, applicant, participant)
        _ <- repository.create(orderToSave)
        _ = mailClient.sendEmail(List(applicant.onesignalId, participant.onesignalId), EmailType.ORDER_CREATED)
      } yield orderToSave
  }

  def validateOrder(order: OrderCreation) = {
    val beginDate = order.availableFrom
    val endDate = order.availableTo

    if(beginDate.before(DateTimeNow.now.minusMinutes(10).toDate) || beginDate.after(endDate))
      throw ShippearException(s"The order has an invalid date range with from: $beginDate and to: $endDate")
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
      _ <- repository.replace(order.copy(state = OrderState.PENDING_CARRIER))
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


  def validateQrCode(orderToValidate: OrderToValidate): Future[Boolean] = {
    for {
      order <- repository.findOneById(orderToValidate.orderId)
      verification = verifyQR(orderToValidate, order)
      _ <- updateOrderStatus(order, orderToValidate.userType, verification)
    } yield verification
  }


  def verifyQR(orderToValidate: OrderToValidate, order: Order) = {
    orderToValidate.userType match{
      case APPLICANT => order.applicant.id.equals(orderToValidate.userId)
      case PARTICIPANT => order.participant.id.equals(orderToValidate.userId)
      case CARRIER =>  order.carrier.getOrElse(throw NotFoundException("Carrier not found")).id.equals(orderToValidate.userId)
    }
  }

  def updateOrderStatus(order: Order, userType: UserType, verification: Boolean): Future[_] = {
    if(verification) {
      val newOrder = userType match {
        case UserType.CARRIER => order.copy(state = OrderState.ON_TRAVEL)
        case _ => order.copy(state = OrderState.DELIVERED, finalizedDate = Some(DateTimeNow.now.toDate), ratedCarrier = Some(false))
      }
      repository.replace(newOrder)
    } else Future.successful(Unit)

  }

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