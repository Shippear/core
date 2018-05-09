package dao.embbebedmongo

import ai.snips.bsonmacros.DatabaseContext
import com.google.inject.Inject
import org.mongodb.scala.MongoDatabase
import play.api.Configuration
import play.api.inject.DefaultApplicationLifecycle
import scala.concurrent.{ExecutionContext, Future}

class DBContext @Inject()(implicit ec: ExecutionContext)
  extends DatabaseContext(Configuration(), new DefaultApplicationLifecycle)
    with Connection {

  override def database(name: String): MongoDatabase =
    mongo.getDatabase(name).withCodecRegistry(codecRegistry)

  override def ping(): Future[Unit] =
    mongo.listDatabaseNames().toFuture.map(_ => ())

}
