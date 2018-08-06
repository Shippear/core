package model.mapper

import java.util.{Date, TimeZone}

import com.github.nscala_time.time.Imports.DateTime
import model.common.IdGenerator
import model.internal.{Order, OrderState, User, UserDataOrder}
import model.request.OrderCreation
import org.joda.time.{Chronology, DateTimeZone, Minutes}

object OrderMapper extends IdGenerator {

  def extractUserData(user: User): UserDataOrder = {
    UserDataOrder(user._id, user.firstName, user.lastName, user.photoUrl, user.onesignalId, user.scoring)
  }

  def orderCreationToOrder(orderCreation: OrderCreation,
                           applicant: User,
                           participant: User,
                           carrier: Option[User] = None): Order = {

    val applicantData = extractUserData(applicant)
    val participantData = extractUserData(participant)
    val carrierData = carrier.map(extractUserData)

    val supportedTransports = Some(orderCreation.supportedTransports.map(_.toString))

    val orderNumber = DateTime.now(DateTimeZone.forID("America/Argentina/Buenos_Aires")).getMillis

    val awaitTo = calculateAwait(orderCreation.availableFrom, orderCreation.availableTo)

    Order(orderCreation._id.getOrElse(generateId),
      applicantData,
      participantData,
      carrierData,
      orderNumber,
      orderCreation.description,
      OrderState.PENDING_PARTICIPANT,
      orderCreation.operationType,
      orderCreation.size,
      orderCreation.weight,
      supportedTransports,
      orderCreation.route,
      orderCreation.availableFrom,
      orderCreation.availableTo,
      awaitTo,
      orderCreation.qrCode,
      orderCreation.ratedCarrier,
      orderCreation.price,
      None
    )

  }

  def calculateAwait(availableFrom: Date, availableTo: Date): Option[Date] = {
    val dateTimeFrom: DateTime = new DateTime(availableFrom)
    val dateTimeTo: DateTime = new DateTime(availableTo)

    val minutes = Minutes.minutesBetween(dateTimeFrom.toDateTime, dateTimeTo.toDateTime).getMinutes * 0.15

    Some(dateTimeFrom.plusMinutes(minutes.toInt).toDate)

  }


}

