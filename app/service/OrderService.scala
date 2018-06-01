package service

import com.google.inject.Inject
import model.internal.{Order, UserType}
import qrcodegenerator.QrCodeGenerator
import repository.{OrderRepository, UserRepository}
import service.Exception.NotFoundException

import scala.concurrent.{ExecutionContext, Future}

class OrderService @Inject()(val repository: OrderRepository, qrCodeGenerator: QrCodeGenerator)(implicit ec: ExecutionContext)
  extends Service[Order]{

  def assignCarrier(content: Map[String , String]) = {
    val orderId = content.get("order_id") match {
      case Some(id) => id
      case _  => throw NotFoundException("Order id doesn't exists")
    }
    val userId = content.getOrElse("user_id", throw NotFoundException("User id doesn't exists"))

    val qrCode = qrCodeGenerator.generateQrImage(orderId).stream().toByteArray
    repository.assignCarrier(orderId, userId, qrCode)
  }

  def validateQrCode(content: Map[String, String]) : Future[Any]  = {
    val orderId = content.getOrElse("order_id", throw NotFoundException("Order id doesn't exists"))
    val userId = content.getOrElse("user_id", throw NotFoundException("User id doesn't exists"))
    val userType = content.getOrElse("user_type", throw  NotFoundException("User type doesn't exists"))

    repository.validateQrCode(orderId,userId, UserType.toState(userType))
  }

  def cancelOrder(id: String): Future[(String, String, Option[String])] =
   repository.cancelOrder(id)

}