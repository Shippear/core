package service


import com.google.inject.Inject
import common.Logging
import dao.UserDAO
import model.{Address, User}

import scala.concurrent.{ExecutionContext, Future}

class UserService @Inject()(val dao: UserDAO)(implicit ec: ExecutionContext) extends Logging
  with Service[User]{

  override def create(user: User): Future[_] = {
    if(validateAddresses(user.addresses))
      super.create(user)
    else
      throw new Exception("There's not a public address")
  }

  override def update(user: User): Future[_] = {
    if(validateAddresses(user.addresses))
      super.update(user)
    else
    throw new Exception("There's not a public address")
  }

  private def validateAddresses(addresses: Seq[Address]) = addresses.exists(_.public)

}
