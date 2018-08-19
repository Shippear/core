package model.mapper

import java.util.Date

import com.github.nscala_time.time.Imports.DateTime
import common.ConfigReader
import model.common.IdGenerator
import model.internal.OperationType._
import model.internal.OrderState._
import model.internal.{Order, Route, User, UserDataOrder}
import model.request.OrderCreation
import org.joda.time.Minutes
import common.DateTimeNow._

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
    //Applicant Data
    val applicantData = extractUserData(applicant, Some(orderCreation.operationType))

    //Participant Data
    val participanRole = if(orderCreation.operationType.equals(SENDER)) RECEIVER else SENDER
    val participantData = extractUserData(participant, Some(participanRole))

    //Carrier Data
    val carrierData = carrier.map(c => extractUserData(c))

    val supportedTransports = orderCreation.supportedTransports.map(_.toString)

    val orderNumber = rightNowTime.getMillis

    val routeWithAwaitTimes = calculateAwaitTimes(orderCreation, orderCreation.operationType)


    val orderTimeout = orderCreation.operationType match {
      case SENDER => calculateOrderTimeout(orderCreation.availableFrom, orderCreation.availableTo)
      case _ => calculateOrderTimeout(routeWithAwaitTimes.origin.awaitFrom.get, routeWithAwaitTimes.origin.awaitTo.get)
    }
      calculateOrderTimeout(orderCreation.availableFrom, orderCreation.availableTo)

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
      routeWithAwaitTimes,
      orderCreation.availableFrom,
      orderCreation.availableTo,
      orderTimeout,
      orderCreation.qrCode,
      orderCreation.ratedCarrier,
      orderCreation.paymentMethod,
      orderCreation.price,
      Some(orderCreation.price * commissionCarrier),
      None
    )

  }

  def calculateOrderTimeout(availableFrom: Date, availableTo: Date): Option[Date] = {
    val dateTimeFrom: DateTime = new DateTime(availableFrom)
    val dateTimeTo: DateTime = new DateTime(availableTo)

    // 0.15 is the predefined percentage of the total awaiting time
    val minutes = Minutes.minutesBetween(dateTimeFrom.toDateTime, dateTimeTo.toDateTime).getMinutes * 0.15

    Some(dateTimeFrom.plusMinutes(minutes.toInt))

  }

  def calculateAwaitTimes(order: OrderCreation, operationType: OperationType): Route = {
    operationType match {
      case SENDER =>
        val originAddress = order.route.origin.copy(awaitFrom = Some(order.availableFrom), awaitTo = Some(order.availableTo))

        val awaitFromParticipant = new DateTime(order.availableFrom).plusSeconds(order.duration.toInt)
        val awaitToParticipant = new DateTime(order.availableTo).plusSeconds(order.duration.toInt)
        val modifiedDestination = order.route.destination.copy(awaitFrom = Some(awaitFromParticipant), awaitTo = Some(awaitToParticipant))

        order.route.copy(origin = originAddress, destination = modifiedDestination)

      case _ =>
        val destinationAddress = order.route.origin.copy(awaitFrom = Some(order.availableFrom), awaitTo = Some(order.availableTo))

        val awaitFromApplicant: Date = new DateTime(order.availableFrom).minusSeconds(order.duration.toInt)
        val awaitToApplicant = new DateTime(order.availableTo).minusSeconds(order.duration.toInt)
        val modifiedOrigin = order.route.origin.copy(awaitFrom = Some(awaitFromApplicant), awaitTo = Some(awaitToApplicant))

        order.route.copy(origin = modifiedOrigin, destination = destinationAddress)
    }
  }


}

