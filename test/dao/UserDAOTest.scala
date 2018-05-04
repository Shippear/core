package dao

import dao.embbebedmongo.MongoTest
import play.api.test.Helpers._


class UserDAOTest extends MongoTest {

  "UserDAO" should {
    "save" in {

      await(dbCollection.insertOne(testObject).toFuture())
      await(dbCollection.count().toFuture()) mustBe  1

    }

  }

}
