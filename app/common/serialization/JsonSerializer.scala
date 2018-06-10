package common.serialization

import java.text.SimpleDateFormat

import akka.util.ByteString
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ser.impl.{SimpleBeanPropertyFilter, SimpleFilterProvider}
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, PropertyNamingStrategy, SerializationFeature, _}
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.github.nscala_time.time.Imports._
import play.api.http.Writeable
import play.api.mvc.Codec

trait Factory[T] {
  def newInstance(): T
}

object JsonSerializer {

  val CatalogFilter = "idOnlyFilter"

  private val mapperFactory = new Factory[JsonSerializerType] {
    def newInstance(): JsonSerializerType = {
      val mapper = new ObjectMapper() with ScalaObjectMapper
      mapper.registerModule(DefaultScalaModule)
      mapper.registerModule(new JodaModule)
      mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"))
      mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
      mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
      mapper.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, true)
      mapper
    }
  }

  val CamelCaseMapper: JsonSerializerType = mapperFactory.newInstance()

  val CamelCaseWriter: ObjectWriter = {
    val mapper = CamelCaseMapper
    val filters = new SimpleFilterProvider().addFilter(CatalogFilter, SimpleBeanPropertyFilter.serializeAllExcept("INVALID_PROPERTY"))
    mapper.writer(filters)
  }

  val SnakeCaseMapper: JsonSerializerType = {
    val mapper = mapperFactory.newInstance()
    mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
    mapper
  }

  val SnakeCaseWriter: ObjectWriter = {
    val mapper = SnakeCaseMapper
    val filters = new SimpleFilterProvider().addFilter(CatalogFilter, SimpleBeanPropertyFilter.serializeAllExcept("INVALID_PROPERTY"))
    mapper.writer(filters)
  }

  val SnakeCaseFilterWriter: ObjectWriter = {
    val mapper = SnakeCaseMapper
    val filters = new SimpleFilterProvider().addFilter(CatalogFilter, SimpleBeanPropertyFilter.filterOutAllExcept("id"))
    mapper.writer(filters)
  }
}

trait CamelCaseJsonProtocol extends JsonProtocol {
  implicit def mapper = JsonSerializer.CamelCaseMapper

  implicit def writer = JsonSerializer.CamelCaseWriter
}

trait SnakeCaseJsonProtocol extends JsonProtocol {
  implicit def mapper = JsonSerializer.SnakeCaseMapper

  implicit def writer = JsonSerializer.SnakeCaseWriter

  val filterWriter = JsonSerializer.SnakeCaseFilterWriter
}

trait JsonProtocol extends DefaultWriteables with JSONContentTypeOfs {
  implicit def mapper: JsonSerializerType

  implicit def writer: ObjectWriter


  implicit def writeableOf_Product[T <: Product](implicit codec: Codec): Writeable[T] = {
    Writeable(obj => codec.encode(obj.toJson))
  }

  implicit def writeableOf_Map[T <: Map[String, _]](implicit codec: Codec): Writeable[T] = {
    Writeable(obj => codec.encode(obj.toJson))
  }

  implicit def writeableOf_DateTime(implicit codec: Codec): Writeable[DateTime] = {
    Writeable(obj => codec.encode(obj.toJson))
  }

  implicit def writeableOf_LocalDate(implicit codec: Codec): Writeable[LocalDate] = {
    Writeable(obj => codec.encode(obj.toJson))
  }

  implicit def writeableOf_Boolean(implicit codec: Codec): Writeable[Boolean] = {
    Writeable(obj => codec.encode(obj.toJson))
  }


}


trait DefaultWriteables extends DefaultContentTypeOfs {

  implicit val writeableOf_Unit: Writeable[Unit] = {
    Writeable(_ => ByteString.empty)
  }
}