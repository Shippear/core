package model.request

import java.util.Date

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import model.common.IdGenerator
import model.internal._

case class OrderCreation(@JsonProperty("_id") _id: Option[String],
                         applicantId: String,
                         participantId: String,
                         description: String,
                         @JsonScalaEnumeration(classOf[OperationTypeType])operationType: OperationType.OperationType,
                         route: Route,
                         availableFrom: Date,
                         availableTo: Date,
                         qrCode: Option[Array[Byte]],
                         ratedCarrier: Option[Boolean])