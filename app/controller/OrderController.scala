package controller

import com.google.inject.Inject
import controller.util.BaseController
import model.internal.{AssignCarrier, Order, OrderToValidate}
import model.request.OrderCreation
import service.OrderService

import scala.concurrent.ExecutionContext

class OrderController @Inject()(service: OrderService)(implicit ec: ExecutionContext)
  extends BaseController {


  def createOrder = AsyncActionWithBody[OrderCreation] { implicit request =>
    service.createOrder(request.content).map { order =>
      info(s"Order ${order._id} created")
      Ok(order)
    }.recover {
      case ex: Exception =>
        constructErrorResult("Error creating a new order", ex)
    }
  }

  def findOrder = AsyncActionWithBody[Map[String, String]] { implicit request =>
    service.findBy(request.content).map {
      order => Ok(order)
    }.recover {
      case ex: Exception =>
        constructErrorResult(s"Error getting order with criteria ${request.content}", ex)
    }
  }

  def updateOrder = AsyncActionWithBody[Order] { implicit request =>
    service.update(request.content).map{
      _ => Ok(Map("result" -> s"Order ${request.content._id} updated successfully"))
    }.recover {
      case ex: Exception =>
        constructErrorResult(s"Error updating order ${request.content._id}", ex)
    }
  }

  def allOrders = AsyncAction { implicit request =>
    service.all.map{ result =>
      Ok(result.toList)
    }.recover {
      case ex: Exception =>
        constructErrorResult(s"Error getting all orders.", ex)
    }
  }

  def cancelOrder(idOrder: String) = AsyncAction { implicit request =>
    service.cancelOrder(idOrder).map {
      _ => Ok(Map("result" -> s"Order $idOrder canceled successfully"))
    }.recover {
      case ex: Exception =>
        constructErrorResult(s"Error cancelling order $idOrder", ex)
    }
  }

  def confirmParticipant(idOrder: String) = AsyncAction { implicit request =>
    service.confirmParticipant(idOrder).map{
      order => Ok(Map("result" -> s"Order ${order._id} assigned to participant ${order._id} successfully"))
    }.recover {
      case ex: Exception =>
        constructErrorResult(s"Error updating order", ex)
    }
  }

  def assignCarrier = AsyncActionWithBody[AssignCarrier] { implicit request =>
    service.assignCarrier(request.content).map{
      order => Ok(Map("result" -> s"Order ${order._id} assigned to carrier ${request.content.carrierId} successfully"))
    }.recover {
      case ex: Exception =>
        constructErrorResult(s"Error updating order", ex)
    }
  }

  def validateQrCode = AsyncActionWithBody[OrderToValidate] { implicit request =>
    service.validateQrCode(request.content).map{
      case true => Ok(Map("result" -> "QR Code validate successfully"))
      case _ => Forbidden(Map("result" -> "Wrong QR Code"))
    }.recover{
      case ex: Exception =>
        constructErrorResult(s"Error to validate QR Code", ex)
    }
  }

}
