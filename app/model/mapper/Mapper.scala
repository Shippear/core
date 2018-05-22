package model.mapper

import model.internal._
import model.request.OrderRequest
import org.mongodb.scala.bson.ObjectId

trait IdGenerator {
  def generateId: String = new ObjectId().toHexString
}

trait OrderMapper extends IdGenerator{

  implicit def request2Reponse(request: OrderRequest): Order =
    Order(request._id.getOrElse(generateId),
          request.applicantId,
          request.participantId,
          request.carrierId,
          request.state,
          request.operationType,
          request.route,
          request.availableFrom,
          request.availableTo,
          request.awaitFrom,
          request.awaitTo,
          request.qrCode)

}