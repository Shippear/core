package model.request

import java.util.Date

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import model.common.IdGenerator
import model.internal._

case class OrderRequest(@JsonProperty("_id") _id: Option[String],
                        applicantId: String,
                        participantId: String,
                        carrierId: Option[String],
                        description: String,
                        @JsonScalaEnumeration(classOf[OrderStateType]) state: OrderState.OrderState,
                        @JsonScalaEnumeration(classOf[OperationTypeType])operationType: OperationType.OperationType,
                        route: Route,
                        availableFrom: Date,
                        availableTo: Date,
                        awaitFrom: Option[Date],
                        awaitTo: Option[Date],
                        qrCode: Option[Array[Byte]],
                        ratedCarrier: Option[Boolean])


object OrderRequest extends IdGenerator {

  implicit def request2Reponse(request: OrderRequest): Order =
    Order(request._id.getOrElse(generateId),
      request.applicantId,
      request.participantId,
      request.carrierId,
      request.description,
      request.state,
      request.operationType,
      request.route,
      request.availableFrom,
      request.availableTo,
      request.awaitFrom,
      request.awaitTo,
      request.qrCode,
      request.ratedCarrier)
}
