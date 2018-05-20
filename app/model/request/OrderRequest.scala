package model.request

import java.util.Date

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import model.internal.OperationType.OperationType
import model.internal._

case class OrderRequest(@JsonProperty("_id") _id: Option[String],
                         applicantId: String,
                         participantId: String,
                         carrierId: Option[String],
                         state: String,
                        @JsonScalaEnumeration(classOf[OperationTypeType])operationType: OperationType.OperationType,
                         route: Route,
                         availableFrom: Date,
                         availableTo: Date,
                         awaitFrom: Option[Date],
                         awaitTo: Option[Date],
                         qrCode: Option[String])
