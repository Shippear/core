package service

import com.google.inject.Inject
import model.internal.{AssignCarrier, Order, OrderState, OrderToValidate}
import onesignal.{EmailType, OneSignalClient}
import qrcodegenerator.QrCodeGenerator
import qrcodegenerator.QrCodeGenerator._
import repository.{OrderRepository, UserRepository}

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
      order <- repository.assignCarrier(content.orderId, content.carrierId, qrCodeGenerator.generateQrImage(content.orderId))
      list = List(order.applicantId, order.participantId) ++ order.carrierId
      _ = mailClient.sendEmail(list, EmailType.ORDER_WITH_CARRIER)
    } yield order


  def validateQrCode(content: OrderToValidate): Future[Boolean] =
    repository.validateQrCode(content.orderId, content.userId, content.userType)
}