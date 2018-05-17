package dao.embbebedmongo

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.PlaySpec

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait MongoTest extends PlaySpec with Connection with BeforeAndAfterEach with BeforeAndAfterAll with MockitoSugar {

  override def beforeAll = mongodExecutable.start

  override def afterAll = mongodExecutable.stop()

  override def afterEach = Await.ready(mongo.getDatabase(config.database).drop().toFuture(), 30 seconds)

  val dbContext = new ShippearDBContextTest

}