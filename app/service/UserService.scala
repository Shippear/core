package service


import com.google.inject.Inject
import common.Logging
import model.internal.{Address, User}
import model.response.UserResponse
import repository.UserRepository

import scala.concurrent.{ExecutionContext, Future}

class UserService @Inject()(val repository: UserRepository)(implicit ec: ExecutionContext) extends Logging
  with Service[User]{

  override def create(user: User): Future[_] = {
    if(validateAddresses(user.addresses))
      super.create(user)
    else
      throw new Exception(s"User ${user.userName} doesn't have a public address")
  }

  override def update(user: User): Future[_] = {
    if(validateAddresses(user.addresses))
      super.update(user)
    else
     throw new Exception(s"User ${user.userName} doesn't have a public address")
  }

  private def validateAddresses(addresses: Seq[Address]) = addresses.exists(_.public)

  def ordersByState(idUser: String): Future[UserResponse] = {
    super.findBy(Map("_id" -> idUser)).map(User.user2Response)
  }



}
