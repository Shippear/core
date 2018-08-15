package onesignal

case class Notification(appId: String,includePlayerIds: List[String], contents: Map[String,String], data: DataNotification)

case class DataNotification(orderId: String, newState: String , photoUrl: String, action: String)


object ActionState extends Enumeration {
  type ActionState = Value

  val RELOAD = Value

  implicit def toString(state: ActionState) = state.toString

  implicit def toState(state: String): ActionState = withName(state.toUpperCase)
}