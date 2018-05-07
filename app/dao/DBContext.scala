package dao

import ai.snips.bsonmacros.{CodecGen, DatabaseContext}
import com.google.inject.Inject
import model._

class DBContext @Inject()(dbContext: DatabaseContext) {

  CodecGen[Transport](dbContext.codecRegistry)
  CodecGen[Geolocation](dbContext.codecRegistry)
  CodecGen[PaymentMethod](dbContext.codecRegistry)
  CodecGen[ContactInfo](dbContext.codecRegistry)
  CodecGen[Address](dbContext.codecRegistry)
  CodecGen[Route](dbContext.codecRegistry)
  CodecGen[Address](dbContext.codecRegistry)
  CodecGen[Order](dbContext.codecRegistry)
  CodecGen[User](dbContext.codecRegistry)

  def database(name: String)  = dbContext.database(name)

}
