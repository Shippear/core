package model.request

import java.util.Date

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import model.internal._
import model.internal.price.enum.{Size, SizeType, Weight, WeightType}

case class OrderCreation(@JsonProperty("_id") _id: Option[String],
                         applicantId: String,
                         participantId: String,
                         description: String,
                         @JsonScalaEnumeration(classOf[OperationTypeType]) operationType: OperationType.OperationType,
                         @JsonScalaEnumeration(classOf[SizeType]) size: Size.Size,
                         @JsonScalaEnumeration(classOf[WeightType]) weight: Weight.Weight,
                         @JsonScalaEnumeration(classOf[TransportTypeType]) supportedTransports: List[TransportType.TransportType],
                         route: Route,
                         availableFrom: Date,
                         availableTo: Date,
                         qrCode: Option[Array[Byte]],
                         ratedCarrier: Option[Boolean],
                         paymentMethod: PaymentMethod,
                         price: Double,
                         duration: Long)