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
      _ = error(s"USER: $user")
      newUser = copyNewUser(user, order)
      _ = error(s"NEW USER: $newUser")
      _ <- dao.replaceOne(newUser)
    } yield newUser
  }


  private def copyNewUser(user: User, newOrder: Order): User = {
    val updatedOrder = Some(replaceOrAdd(user.orders.getOrElse(Seq.empty), newOrder){ _._id==newOrder._id })
    user.copy(orders = updatedOrder)
  }


}

