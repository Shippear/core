package dao.embbebedmongo

import ai.snips.bsonmacros.DatabaseContext
import com.google.inject.Inject
import dao.ShippearDBContext
import org.mongodb.scala.MongoDatabase
import play.api.Configuration
import play.api.inject.DefaultApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}

class ShippearDBContextTest @Inject()(implicit ec: ExecutionContext)
  extends ShippearDBContext(new DefaultApplicationLifecycle) with Connection {


  override def database(name: String): MongoDatabase =
    mongo.getDatabase(name).withCodecRegistry(codecRegistry)


}
