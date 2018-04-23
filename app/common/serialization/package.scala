package common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import common.serialization.{JsonSerializer, JsonSerializerType}

package object serialization {

  type JsonSerializerType = ObjectMapper with ScalaObjectMapper

  implicit def pimpAny[T](any: T): PimpedAny[T] = new PimpedAny(any)
  implicit def pimpString(string: String): PimpedString = new PimpedString(string)
}

import com.fasterxml.jackson.databind.ObjectWriter

  protected class PimpedAny[T](any: T) {
    def toJson(implicit writer: ObjectWriter): String = writer.writeValueAsString(any)

    def toSnakeCase: String = toJson(JsonSerializer.SnakeCaseWriter)

    def toCamelCase: String = toJson(JsonSerializer.CamelCaseWriter)
  }

  protected class PimpedString(string: String) {
    def parseJsonTo[T: Manifest](implicit mapper: JsonSerializerType): T = mapper.readValue[T](string)
  }
