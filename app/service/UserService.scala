package service


import com.google.inject.Inject
import common.Logging
import model.internal.User
import model.response.UserResponse
import repository.UserRepository
import service.Exception.{NotFoundException, ShippearException}
import service.Exception.BadRequestCodes._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext, Future}

class UserService @Inject()(val repository: UserRepository)(implicit ec: ExecutionContext) extends Logging
  with Service[User]{

  val UserName = "userName"

  override def create(user: User): Future[_] = validateAndExecute(super.create, user, {
    val causes = ArrayBuffer.empty[String]

    repository.findOneBy(Map(UserName -> user.userName)).map { userFound =>
       causes.+=(s"username ${userFound.userName} already exists")
      causes
    }.recover{
      case ex: NotFoundException =>
        if(!user.addresses.exists(_.public))
          causes.+=("doesn't have a public address")

      causes
    }})

  override def update(user: User): Future[_] = validateAndExecute(super.update, user, {
    val causes = ArrayBuffer.empty[String]

   repository.findOneBy(Map(UserName -> user.userName)).map { userFound =>
     if (!userFound._id.equals(user._id) && userFound.userName.equals(user.userName))
       causes.+=(s"username ${user.userName} already exists")

     if (!user.addresses.exists(_.public))
       causes.+=("doesn't have a public address")

     causes
   }
  })

  private def validateAndExecute(execute: User => Future[_], user: User, validationResult: Future[ArrayBuffer[String]]) = {
    validationResult.flatMap { validationResult =>
      if(validationResult.isEmpty)
        execute(user)
      else
        throw ShippearException(ValidationError ,s"${validationResult.mkString(", ")}.")
    }
  }

  def ordersByState(idUser: String): Future[UserResponse] =
    super.findById(idUser).map(User.user2Response)

}
