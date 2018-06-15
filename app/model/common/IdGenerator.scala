package model.common

import org.mongodb.scala.bson.ObjectId

trait IdGenerator {
  def generateId: String = new ObjectId().toHexString
}
