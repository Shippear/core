package model.mapper

import model.common.IdGenerator
import model.internal.{Order, OrderState, User, UserDataOrder}
import model.request.OrderCreation
import org.joda.time.DateTime

object OrderMapper extends IdGenerator {

  def extractUserData(user: User): UserDataOrder = {
    UserDataOrder(user._id, user.firstName, user.lastName, user.photoUrl, user.onesignalId)
  }

  def orderCreationToOrder(orderCreation: OrderCreation,
                           applicant: User,
                           participant: User,
                           carrier: Option[User] = None): Order = {

    val applicantData = extractUserData(applicant)
    val participantData = extractUserData(participant)
    val carrierData = carrier.map(extractUserData)

    val orderNumber = DateTime.now().getMillis

    Order(orderCreation._id.getOrElse(generateId),
      applicantData,
      participantData,
      carrierData,
      orderNumber,
      orderCreation.description,
      OrderState.PENDING_PARTICIPANT,
      orderCreation.operationType,
      orderCreation.route,
      orderCreation.availableFrom,
      orderCreation.availableTo,
      orderCreation.awaitFrom,
      orderCreation.awaitTo,
      orderCreation.qrCode,
      orderCreation.ratedCarrier,
      None
    )

  }


}
