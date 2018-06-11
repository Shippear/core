package service

import com.google.inject.Inject
import model.internal.{AssignCarrier, Order, OrderToValidate}
import onesignal.{EmailType, OneSignalClient}
import qrcodegenerator.QrCodeGenerator
import repository.OrderRepository

import scala.concurrent.{ExecutionContext, Future}

class OrderService @Inject()(val repository: OrderRepository, mailClient: OneSignalClient, qrCodeGenerator: QrCodeGenerator)(implicit ec: ExecutionContext)
  extends Service[Order]{

  def cancelOrder(id: String): Future[(String, String, Option[String])] =
    for {
      (applicantOSId, participantOSId, carrierOSId) <- repository.cancelOrder(id)
      list = List(applicantOSId, participantOSId, carrierOSId.getOrElse("")).filter(_.isEmpty)
      _ = mailClient.sendEmail(list, EmailType.ORDER_CANCELED)
    } yield (applicantOSId, participantOSId, carrierOSId)


  def assignCarrier(content: AssignCarrier) = {
    val qrCode = qrCodeGenerator.generateQrImage(content.orderId).stream().toByteArray
    repository.assignCarrier(content.orderId, content.userId, qrCode)
  }

  def validateQrCode(content: OrderToValidate): Future[Any] = repository.validateQrCode(content.orderId, content.userId, content.userType)


  def sendEmail(oneSignal: String, typeMail: String) = mailClient.sendEmail(List(oneSignal), typeMail)

  def device(userOneSignalId: Option[String]) = mailClient.device(userOneSignalId)

}