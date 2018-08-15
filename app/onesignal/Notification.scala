package onesignal

import model.internal.OrderState.{OrderState, Value, withName}
import onesignal.ActionState.ActionState

case class Notification(appId: String,includePlayerIds: List[String], contents: Map[String,String], data:DataNotification)


case class DataNotification(orderId:  String, newState: OrderState , photoUrl: String, action: ActionState)


object ActionState extends Enumeration {
  type ActionState = Value

  val RELOADED = Value

  implicit def toString(state: ActionState) = state.toString

  implicit def toState(state: String): ActionState = withName(state.toUpperCase)
}