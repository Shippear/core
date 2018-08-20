package service.Exception

object BadRequestCodes {

  val IllegalArgument = 400

  val ValidationError = 100
  val InvalidParticipantDateRange = 101

  val InvalidOrderState = 103

  val CarrierWithMoreOrders = 104
  val CarrierAlreadyRated = 105

  val OrderWithoutCarrier = 106

  val NotificationException = 107

}
