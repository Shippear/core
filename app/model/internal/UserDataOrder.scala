package model.internal

import java.util.Date

case class UserDataOrder(id: String,
                         firstName: String,
                         lastName: String,
                         birthDate: Date,
                         contactInfo: ContactInfo,
                         photoUrl: String,
                         oneSignalId: String,
                         scoring: Option[Float],
                         role: Option[String])
