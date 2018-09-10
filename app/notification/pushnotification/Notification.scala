package notification.pushnotification

case class Notification(appId: String,
                        includePlayerIds: List[String],
                        contents: Map[String,String],
                        data: DataNotification,
                        largeIcon: String = ShippearLogo.logo)

object ActionState extends Enumeration {
  type ActionState = Value

  val RELOAD = Value

  implicit def toString(state: ActionState) = state.toString

  implicit def toState(state: String): ActionState = withName(state.toUpperCase)
}

case class DataNotification(orderId: String, newState: String , photoUrl: String, action: String, silent: Boolean)

object ShippearLogo {
  val logo = "https://firebasestorage.googleapis.com/v0/b/shippear-f6914.appspot.com/o/logo%2Flogo%20with%20circle.png?alt=media&token=058dc89d-4ce9-47df-a9e1-dea8140d6bb7"
}
