package controller

import com.google.inject.Inject
import controller.util.BaseController
import model.Order
import service.OrderService

import scala.concurrent.{ExecutionContext, Future}

class OrderController @Inject()(service: OrderService)(implicit ec: ExecutionContext) extends BaseController {


  def createOrder = AsyncActionWithBody[Order] { implicit request =>
    service.create(request.content).map { _ =>
      info(s"Order ${request.content._id} created")
      Ok(Map("result" -> s"Order ${request.content._id} created"))
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


}
