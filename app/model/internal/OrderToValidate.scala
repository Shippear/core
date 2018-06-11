package model.internal

import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import model.internal.UserType.UserType

case class OrderToValidate (orderId: String,
                            userId: String,
                            @JsonScalaEnumeration(classOf[UserTypeType]) userType: UserType
                         )
