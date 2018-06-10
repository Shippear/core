package service

import com.google.inject.Inject
import model.internal.{AssignCarrier, Order, OrderToValidate, UserType}
import qrcodegenerator.QrCodeGenerator
import repository.{OrderRepository, UserRepository}
import service.Exception.NotFoundException

import scala.concurrent.{ExecutionContext, Future}

class OrderService @Inject()(val repository: OrderRepository, qrCodeGenerator: QrCodeGenerator)(implicit ec: ExecutionContext)
  extends Service[Order]{

  def assignCarrier(content: AssignCarrier) = {
    val qrCode = qrCodeGenerator.generateQrImage(content.orderId).stream().toByteArray
    repository.assignCarrier(content.orderId, content.userId, qrCode)
  }

  def validateQrCode(content: OrderToValidate) : Future[Any]  = {
    repository.validateQrCode(content.orderId, content.userId, content.userType)
  }

  def cancelOrder(id: String): Future[(String, String, Option[String])] =
   repository.cancelOrder(id)

}