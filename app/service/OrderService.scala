package service

import java.util.Date

import com.google.inject.Inject
import common.DateTimeNow._
import model.internal.OperationType._
import model.internal.OrderState._
import model.internal.UserType._
import model.internal._
import model.mapper.OrderMapper
import model.request.{CancelOrder, CarrierRating, OrderCreation}
import onesignal.EventType._
import onesignal.OneSignalClient
import qrcodegenerator.QrCodeGenerator
import qrcodegenerator.QrCodeGenerator._
import repository.{OrderRepository, UserRepository}
import service.Exception.{NotFoundException, ShippearException}
import service.Exception.BadRequestCodes._

import scala.concurrent.{ExecutionContext, Future}

class OrderService @Inject()(val repository: OrderRepository, oneSignalClient: OneSignalClient,
                             qrCodeGenerator: QrCodeGenerator, userRepository: UserRepository)
                            (implicit ec: ExecutionContext) extends Service[Order]{

  def createOrder(newOrder: OrderCreation) = {
    validateAvailableTimes(newOrder)
      for {
        applicant <- userRepository.findOneById(newOrder.applicantId)
        participant <- userRepository.findOneById(newOrder.participantId)
        orderToSave = OrderMapper.orderCreationToOrder(newOrder, applicant, participant)
        _ <- repository.create(orderToSave)
        _ = oneSignalClient.sendNotification(orderToSave, ORDER_CREATED)
      } yield orderToSave
  }

  def validateAvailableTimes(order: OrderCreation) = {
    val beginDate = order.availableFrom
    val endDate = order.availableTo
    val rightNow: Date = rightNowTime.minusMinutes(5)
    val genericMessage = "The order has an invalid date range."

    if(beginDate.after(endDate))
      throw ShippearException(ValidationError, s"$genericMessage Begin date: $beginDate can't be more than the end date: $endDate")

    order.operationType match {
      case SENDER =>
        if(beginDate.before(rightNow))
          throw ShippearException(ValidationError, s"$genericMessage Begin date: $beginDate can't be set before than right now: $rightNow")
      case _ =>
        val participantBeginDate: Date = fromDate(beginDate).minusSeconds(order.duration.toInt)
        if(participantBeginDate.before(rightNow))
          throw ShippearException(InvalidParticipantDateRange, s"$genericMessage Begin date of participant: $participantBeginDate. Can't be set before than right now: $rightNow")
    }

  }


  def cancelOrder(cancelOrder: CancelOrder): Future[_] =
    for {
      order <- repository.findOneById(cancelOrder.orderId)
      _ = validateCancelOrder(order, cancelOrder.userType)
      canceledOrder <- repository.cancelOrder(order)
      _ = oneSignalClient.sendNotification(canceledOrder, ORDER_CANCELED, Some(cancelOrder.userType))
    } yield order


  private def validateCancelOrder(order: Order, userType: UserType) = {
    val message = s"Order is in state ${order.state}"
    val possibleStates = List(PENDING_CARRIER, PENDING_PARTICIPANT, PENDING_PICKUP)
      userType match {
        case APPLICANT | PARTICIPANT =>
          if(!possibleStates.contains(OrderState.toState(order.state)))
            throw ShippearException(InvalidOrderState, message)
        case _ => if(!order.state.equals(PENDING_PICKUP.toString))
            throw ShippearException(InvalidOrderState, message)
      }
  }

  def confirmParticipant(orderId: String): Future[Order] = {
    for {
      order <- repository.findOneById(orderId)
      _ = validateOrderState(order.state, PENDING_PARTICIPANT)
      updatedOrder = order.copy(state = PENDING_CARRIER)
      _ <- repository.update(updatedOrder)
      _ = oneSignalClient.sendNotification(updatedOrder, CONFIRM_PARTICIPANT)
    } yield order
  }

  def assignCarrier(content: AssignCarrier) =
    for {
      carrier <- userRepository.findOneById(content.carrierId)
      _ = validateCarrier(carrier)
      order <- repository.findOneById(content.orderId)
      _ = validateOrderState(order.state, PENDING_CARRIER)
      newOrder <- repository.assignCarrier(order, carrier, qrCodeGenerator.generateQrImage(content.orderId))
      _ = oneSignalClient.sendNotification(newOrder, ORDER_WITH_CARRIER)
    } yield newOrder


  def validateQrCode(orderToValidate: OrderToValidate): Future[Boolean] = {
    val eventType = orderToValidate.userType match {
      case CARRIER => ORDER_ON_WAY
      case _ => ORDER_FINALIZED
    }

    for {
      order <- repository.findOneById(orderToValidate.orderId)
      verification = verifyQR(orderToValidate, order)
      updatedOrder <- updateOrderStatus(order, orderToValidate.userType, verification)
      _ = oneSignalClient.sendNotification(updatedOrder, eventType)
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

  def updateOrderStatus(order: Order, userType: UserType, verification: Boolean): Future[Order] = {
    if(verification) {
      val newOrder = userType match {
        case CARRIER => order.copy(state = ON_TRAVEL)
        case _ => order.copy(state = DELIVERED, finalizedDate = Some(rightNowTime), ratedCarrier = Some(false))
      }

      for {
        _ <- repository.update(newOrder)
      } yield newOrder
    }
    else Future.successful(order)

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

        if(assigned.size == 3) throw ShippearException(CarrierWithMoreOrders, s"Carrier with id ${carrier._id} already has 3 orders assigned")
      case _ => ()
    }

  def validateOrderState(orderState: OrderState, expectingState: OrderState) = {
    if(!orderState.equals(expectingState)) throw ShippearException(InvalidOrderState, s"Order must be in state $expectingState, not in $orderState")
  }

  def rateCarrier(carrierRating: CarrierRating): Future[User] = {
    for{
      order <- repository.findOneById(carrierRating.idOrder)
      _ = validateRating(order)
      carrier <- userRepository.findOneById(order.carrier.getOrElse(throw ShippearException(OrderWithoutCarrier, s"Order ${carrierRating.idOrder} doesn't have a carrier!")).id)
      updatedCarrier = updateCarrierRating(carrier, carrierRating.score)
      _ <- userRepository.update(updatedCarrier)
      _ <- repository.update(order.copy(ratedCarrier = Some(true), ratedValue = Some(carrierRating.score)))
    } yield updatedCarrier
  }

  def validateRating(order: Order) ={
    validateOrderState(DELIVERED, order.state)
    order.ratedCarrier.foreach{ rated =>
      if(rated) throw ShippearException(CarrierAlreadyRated, s"Carrier of order ${order._id} was already rated!")
    }

  }

  def updateCarrierRating(carrier: User, score: Int): User = {

    val result: Option[Double] = carrier.orders.map{ carrierOrders =>
      val delivered = carrierOrders.filter(order => order.state.equals(DELIVERED.toString) && order.ratedCarrier.getOrElse(false))
      val previousAmount = delivered.foldLeft(0)(_ + _.ratedValue.getOrElse(0))

      (previousAmount + score).toDouble / (delivered.length + 1)

      }

    carrier.copy(scoring = result)

  }
}