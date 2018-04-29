package model

import org.mongodb.scala.bson.ObjectId


case class User(_id: String, userName: String, firstName: String, lastName: String, photoId: String, order: Option[Order])