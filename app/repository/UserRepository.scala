package repository

import com.google.inject.Inject
import dao.util.ShippearDAO
import model.{Order, User}

import scala.concurrent.ExecutionContext

class UserRepository @Inject()(implicit ec: ExecutionContext) extends ShippearRepository[User]{

  override def collectionName: String = "users"

  override lazy val dao: ShippearDAO[User] = daoFactory[User](collectionName)

  def updateUserOrder(id: String, order: Order) = {
    for {
      user <- dao.findOneById(id)
      newUser = copyNewUser(user, order)
      _ = dao.replaceOne(newUser)
    } yield newUser
  }


  private def copyNewUser(user: User, newOrder: Order): User = {
    val updatedOrder = Some(replace(user.orders.getOrElse(Seq.empty), newOrder, newOrder._id.equals))
    user.copy(orders = updatedOrder)
  }


}

