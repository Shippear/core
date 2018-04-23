package common.serialization

import com.github.nscala_time.time.Imports._
import play.api.http.{ContentTypeOf, ContentTypes}

trait JSONContentTypeOfs extends DefaultContentTypeOfs {

  implicit def contentTypeOf_Product[T <: Product]: ContentTypeOf[T] = {
    ContentTypeOf[T](Some(ContentTypes.JSON))
  }

  implicit def contentTypeOf_Map[T <: Map[String, _]]: ContentTypeOf[T] = {
    ContentTypeOf[T](Some(ContentTypes.JSON))
  }

  implicit def contentTypeOf_LocalDate: ContentTypeOf[LocalDate] = {
    ContentTypeOf[LocalDate](Some(ContentTypes.JSON))
  }

  implicit def contentTypeOf_DateTime: ContentTypeOf[DateTime] = {
    ContentTypeOf[DateTime](Some(ContentTypes.JSON))
  }

  implicit def contentTypeOf_Boolean: ContentTypeOf[Boolean] = {
    ContentTypeOf[Boolean](Some(ContentTypes.JSON))
  }

}

trait DefaultContentTypeOfs {

  implicit def contentTypeOf_Unit:ContentTypeOf[Unit] = {
    ContentTypeOf[Unit](None)
  }

}

