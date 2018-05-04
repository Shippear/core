package dao.embbebedmongo

import common.Logging
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.PlaySpec

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait MongoTest extends PlaySpec with MongoInit with Connection with BeforeAndAfterEach with BeforeAndAfterAll with Logging {

  override def beforeAll = mongodExecutable.start

  override def afterAll = mongodExecutable.stop()

  override def afterEach = Await.result(client.getDatabase("test").drop().toFuture(), 30 seconds)


}


