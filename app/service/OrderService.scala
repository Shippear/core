package service

import java.io.FileInputStream

import com.google.inject.Inject
import com.mongodb.gridfs.GridFS
import model.{Order, User}
import model.OrderState._
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

    val qrCode = qrCodeGenerator.generateQrImage(orderId).stream()
    repository.assignCarrier(orderId, userId, qrCode.toString)
  }


  def cancelOrder(id: String): Future[(String, String, Option[String])] =
   repository.cancelOrder(id)



}