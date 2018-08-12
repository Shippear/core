package service

import com.google.inject.Inject
import common.DateTimeNow
import model.internal._
import model.internal.OrderState._
import model.internal.UserType._
import model.mapper.OrderMapper
import model.request.{CancelOrder, CarrierRating, OrderCreation}
import onesignal.OneSignalClient
import onesignal.EmailType._
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
        _ = mailClient.sendEmail(List(applicant.onesignalId, participant.onesignalId), ORDER_CREATED)
      } yield orderToSave
  }

  def validateOrder(order: OrderCreation) = {
    val beginDate = order.availableFrom
    val endDate = order.availableTo

    if(beginDate.before(DateTimeNow.now.minusMinutes(10).toDate) || beginDate.after(endDate))
      throw ShippearException(s"The order has an invalid date range with from: $beginDate and to: $endDate")
  }


  def cancelOrder(cancelOrder: CancelOrder): Future[_] =
    for {
      order <- repository.findOneById(cancelOrder.orderId)
      _ = validateCancelOrder(order, cancelOrder.userType)
      _ <- repository.cancelOrder(order)
    } yield order

  private def validateCancelOrder(order: Order, userType: UserType) = {
    val message = s"Order is in state ${order.state}"
    val possibleStates = List(PENDING_CARRIER, PENDING_PARTICIPANT, PENDING_PICKUP)
      userType match {
        case APPLICANT | PARTICIPANT =>
          if(!possibleStates.contains(OrderState.toState(order.state)))
            throw ShippearException(message)
        case _ => if(!order.state.equals(PENDING_PICKUP.toString))
            throw ShippearException(message)
      }
  }

  def confirmParticipant(orderId: String): Future[Order] = {
    for {
      order <- repository.findOneById(orderId)
      _ = validateOrderState(order.state, PENDING_PARTICIPANT)
      _ <- repository.update(order.copy(state = PENDING_CARRIER))
    } yield order
  }

  def assignCarrier(content: AssignCarrier) =
    for {
      carrier <- userRepository.findOneById(content.carrierId)
      _ = validateCarrier(carrier)
      order <- repository.findOneById(content.orderId)
      _ = validateOrderState(order.state, PENDING_CARRIER)
      newOrder <- repository.assignCarrier(order, carrier, qrCodeGenerator.generateQrImage(content.orderId))
      _ = mailClient.sendEmail(List(newOrder.applicant.oneSignalId, newOrder.participant.oneSignalId, carrier.onesignalId), ORDER_WITH_CARRIER)
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
      case APPLICANT =>
        order.state.equals(ON_TRAVEL.toString) && order.applicant.id.equals(orderToValidate.userId)
      case PARTICIPANT =>
        order.state.equals(ON_TRAVEL.toString) && order.participant.id.equals(orderToValidate.userId)
      case CARRIER =>
       order.carrier.getOrElse(throw NotFoundException("Carrier not found")).id.equals(orderToValidate.userId) &&
         order.state.equals(PENDING_PICKUP.toString)
    }
  }

  def updateOrderStatus(order: Order, userType: UserType, verification: Boolean): Future[_] = {
    if(verification) {
      val newOrder = userType match {
        case CARRIER => order.copy(state = ON_TRAVEL)
        case _ => order.copy(state = DELIVERED, finalizedDate = Some(DateTimeNow.now.toDate), ratedCarrier = Some(false))
      }
      repository.update(newOrder)
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
                (orderState.equals(ON_TRAVEL) || orderState.equals(PENDING_PICKUP))
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

  def rateCarrier(carrierRating: CarrierRating): Future[User] = {
    for{
      order <- repository.findOneById(carrierRating.idOrder)
      _ = validateRating(order)
      carrier <- userRepository.findOneById(order.carrier.getOrElse(throw ShippearException(s"Order ${carrierRating.idOrder} doesn't have a carrier!")).id)
      updatedCarrier = updateCarrierRating(carrier, carrierRating.score)
      _ <- userRepository.update(updatedCarrier)
      _ <- repository.update(order.copy(ratedCarrier = Some(true)))
    } yield updatedCarrier
  }

  def validateRating(order: Order) ={
    validateOrderState(DELIVERED, order.state)
    order.ratedCarrier.foreach{ rated =>
      if(rated) throw ShippearException(s"Carrier of order ${order._id} was already rated!")
    }

  }

  def updateCarrierRating(carrier: User, score: Int): User = {
    val carrierScore = carrier.scoring.getOrElse(0.0)

    val result = carrier.orders
      .map{ carrierOrders =>
        val delivered = carrierOrders.filter(order => order.state.equals(DELIVERED.toString))

        (carrierScore.toDouble + score) / delivered.length
      }

    carrier.copy(scoring = result)

  }
}