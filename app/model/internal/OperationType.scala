package model.internal

import com.fasterxml.jackson.core.`type`.TypeReference

object OperationType extends Enumeration {
  type OperationType = Value

  val SENDER, RECEIVER = Value

  implicit def toString(state: OperationType) = state.toString

  implicit def toState(state: String): OperationType = withName(state.toUpperCase)

}

//For jackson deserialization
class OperationTypeType extends TypeReference[OperationType.type]
