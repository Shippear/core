package dao.embbebedmongo

import com.google.inject.Inject
import dao.ShippearDBContext
import play.api.inject.DefaultApplicationLifecycle

import scala.concurrent.ExecutionContext

class ShippearDBContextTest @Inject()(implicit ec: ExecutionContext)
  extends ShippearDBContext(new DefaultApplicationLifecycle) with Connection