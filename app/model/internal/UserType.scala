package model.internal

object UserType extends Enumeration {
    type UserType = Value

    val APPLICANT, PARTICIPANT, CARRIER = Value

    implicit def toString(state: UserType): String = state.toString

    implicit def toState(state: String): UserType = withName(state.toUpperCase)
}
