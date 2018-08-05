package model.mapper

import java.util.Date

import com.github.nscala_time.time.Imports.DateTime
import model.common.IdGenerator
import model.internal.{Order, OrderState, User, UserDataOrder}
import model.request.OrderCreation
import org.joda.time.Minutes

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

    val awaitTo = calculateAwait(orderCreation.availableFrom, orderCreation.availableTo)

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
      awaitTo,
      orderCreation.qrCode,
      orderCreation.ratedCarrier,
      None
    )

  }

  def calculateAwait(availableFrom: Date, availableTo: Date): Option[Date] = {
    val dateTimeFrom: DateTime = new DateTime(availableFrom)
    val dateTimeTo: DateTime = new DateTime(availableTo)

    val minutes = Minutes.minutesBetween(dateTimeFrom.toDateTime, dateTimeTo.toDateTime)

    Some(dateTimeFrom.plusMinutes(minutes.getMinutes).toDate)

  }


}

