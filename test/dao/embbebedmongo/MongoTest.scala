package dao.embbebedmongo

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.PlaySpec
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait MongoTest extends PlaySpec with Connection with BeforeAndAfterEach with BeforeAndAfterAll {

  override def beforeAll = mongodExecutable.start

  override def afterAll = mongodExecutable.stop()

  override def afterEach = Await.result(mongo.getDatabase(config.database).drop().toFuture(), 30 seconds)

  val dbContext = new DBContext

}