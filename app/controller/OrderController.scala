package controller

import com.google.inject.Inject
import controller.util.BaseController
import model.internal.Order
import model.mapper.OrderMapper
import model.request.OrderRequest
import service.OrderService

import scala.concurrent.{ExecutionContext, Future}

class OrderController @Inject()(service: OrderService)(implicit ec: ExecutionContext)
  extends BaseController with OrderMapper {


  def createOrder = AsyncActionWithBody[OrderRequest] { implicit request =>
    val order: Order = request.content
    service.create(order).map { _ =>
      info(s"Order ${order._id} created")
      Ok(Map("_id" -> s"${order._id}"))
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
      _ => Ok(s"Order ${request.content._id} updated successfully")
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
      case (aId, pId, cId) =>
        val OneSignalId = "OneSignalId"
        Ok(cId.foldLeft(Map(s"applicant$OneSignalId" -> aId, s"participant$OneSignalId" -> pId)) {
          case (map, signalId) => map + (s"carrier$OneSignalId" -> signalId)
        })
    }.recover {
      case ex: Exception =>
        constructErrorResult(s"Error cancelling order $idOrder", ex)
    }
  }

  def assignCarrier = AsyncActionWithBody[Map[String,String]] {implicit request =>
    service.assignCarrier(request.content).map{
      _ => Ok(Map("result" -> s"Order assigned to carrier successfully"))
    }.recover {
      case ex: Exception =>
        constructErrorResult(s"Error updating order", ex)
    }
  }

  def validateQrCode = AsyncActionWithBody[Map[String,String]] {implicit request =>
    service.validateQrCode(request.content).map{
      case true => Ok(Map("result" -> s"QR Code validate successfully"))
      case false => Forbidden(Map("result" -> s"Wrong QR Code"))
    }.recover{
      case ex: Exception =>
        constructErrorResult(s"Error to validate Qr Code", ex)
    }
  }

}
