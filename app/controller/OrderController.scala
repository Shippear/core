package controller

import com.google.inject.Inject
import controller.util.BaseController
import model.Order
import service.OrderService

import scala.concurrent.ExecutionContext

class OrderController @Inject()(service: OrderService)(implicit ec: ExecutionContext) extends BaseController {


  def createOrder = AsyncActionWithBody[Order] { implicit request =>
    service.create(request.content).map { _ =>
      info(s"Order ${request.content._id} created")
      Ok(Map("result" -> s"Order ${request.content._id} created"))
    }.recover {
      case ex: Exception =>
        constructInternalError("Error creating a new order", ex)
    }
  }

  def findOrder = AsyncActionWithBody[Map[String, String]] { implicit request =>
    service.findBy(request.content).map {
      user => if(user.isDefined) Ok(user) else NotFound(s"Order with criteria ${request.content} not found")
    }.recover {
      case ex: Exception =>
        constructInternalError(s"Error getting order with criteria ${request.content}", ex)
    }
  }

  def updateOrder = AsyncActionWithBody[Order] { implicit request =>
    service.update(request.content).map{
      _ => Ok(s"Order ${request.content._id} updated successfully")
    }.recover {
      case ex: Exception =>
        constructInternalError(s"Error updating order ${request.content._id}", ex)
    }
  }

  def allOrders = AsyncAction { implicit request =>
    service.all.map{ result =>
      Ok(result.toList)
    }.recover {
      case ex: Exception =>
        constructInternalError(s"Error getting all orders.", ex)
    }
  }


}
