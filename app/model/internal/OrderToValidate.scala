package model.internal

import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import model.internal.UserType.UserType
import play.api.libs.json.Json

case class OrderToValidate (orderId: String,
                            userId: String,
                            @JsonScalaEnumeration(classOf[UserTypeType]) userType: UserType
                         )

object OrderToValidate {
  implicit val jsonFormat = Json.writes[OrderToValidate]
}