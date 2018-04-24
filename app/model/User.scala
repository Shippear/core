package model

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonIgnoreProperties}
import org.mongodb.scala.bson.BsonObjectId

@JsonIgnoreProperties(Array("id"))
case class User(_id: BsonObjectId = BsonObjectId(), userName: String, firstName: String, lastName: String, photoId: String)
