package model.mapper

import java.util.Date

import com.github.nscala_time.time.Imports.DateTime
import common.{ConfigReader, DateTimeNow}
import model.common.IdGenerator
import model.internal.OperationType._
import model.internal.OrderState._
import model.internal.{Order, User, UserDataOrder}
import model.request.OrderCreation
import org.joda.time.Minutes

object OrderMapper extends IdGenerator with ConfigReader {

  def commissionCarrier: Double = envConfiguration.getDouble("commission") / 100

  def extractUserData(user: User, operationType: Option[String] = None): UserDataOrder = {
    UserDataOrder(user._id,
      user.firstName,
      user.lastName,
      user.birthDate,
      user.contactInfo,
      user.photoUrl,
      user.onesignalId,
      user.scoring,
      operationType)
  }

  def orderCreationToOrder(orderCreation: OrderCreation,
                           applicant: User,
                           participant: User,
                           carrier: Option[User] = None): Order = {

    val applicantData = extractUserData(applicant, Some(orderCreation.operationType))

    val participanRole = if(orderCreation.operationType.equals(SENDER)) RECEIVER else SENDER
    val participantData = extractUserData(participant, Some(participanRole))
    val carrierData = carrier.map(c => extractUserData(c))

    val supportedTransports = orderCreation.supportedTransports.map(_.toString)

    val orderNumber = DateTimeNow.now.getMillis

    val awaitTo = calculateAwait(orderCreation.availableFrom, orderCreation.availableTo)

    Order(orderCreation._id.getOrElse(generateId),
      applicantData,
      participantData,
      carrierData,
      orderNumber,
      orderCreation.description,
      PENDING_PARTICIPANT,
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
      orderCreation.paymentMethod,
      orderCreation.price,
      Some(orderCreation.price * commissionCarrier),
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

