package service

import com.google.inject.Inject
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
      order <- repository.assignCarrier(content.orderId, carrier, qrCodeGenerator.generateQrImage(content.orderId))
      list = List(order.applicantId, order.participantId) ++ order.carrierId
      _ = mailClient.sendEmail(list, EmailType.ORDER_WITH_CARRIER)
    } yield order


  def validateQrCode(content: OrderToValidate): Future[Boolean] =
    repository.validateQrCode(content.orderId, content.userId, content.userType)

  def validateCarrier(carrier: User): Unit =
    carrier.orders match {
      case Some(list) =>
        val assigned = list.filter{
          order => order.carrierId.getOrElse("").equals(carrier._id) &&
            order.state.equals(OrderState.ON_TRAVEL.toString)
        }

        if(assigned.size > 3) throw ShippearException(s"Carrier with id ${carrier._id} already has 3 orders assigned")
      case _ => ()
    }
}