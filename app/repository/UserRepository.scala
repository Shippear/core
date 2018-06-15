package repository

import com.google.inject.Inject
import dao.util.ShippearDAO
import model.internal.{Order, User}

import scala.concurrent.ExecutionContext

class UserRepository @Inject()(implicit ec: ExecutionContext) extends ShippearRepository[User]{

  override def collectionName: String = "users"

  override lazy val dao: ShippearDAO[User] = daoFactory[User](collectionName)

  def updateUserOrder(id: String, order: Order) = {
    for {
      user <- dao.findOneById(id)
      _ <- dao.replaceOne(copyNewUser(user, order))
    } yield user
  }


  private def copyNewUser(user: User, newOrder: Order): User = {
    val updatedOrder = Some(replaceOrAdd(user.orders.getOrElse(Seq.empty), newOrder){ _._id==newOrder._id })
    user.copy(orders = updatedOrder)
  }


}

