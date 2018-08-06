package model.internal

case class UserDataOrder(id: String,
                         firstName: String,
                         lastName: String,
                         photoUrl: String,
                         oneSignalId: String,
                         scoring: Option[Double])
