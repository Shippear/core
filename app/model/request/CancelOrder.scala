package model.request

import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import model.internal.UserType.UserType
import model.internal.UserTypeType

case class CancelOrder(orderId: String,
                       @JsonScalaEnumeration(classOf[UserTypeType]) userType: UserType)
