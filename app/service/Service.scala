package service

import common.serialization.CamelCaseJsonProtocol
import repository.ShippearRepository

trait Service[T] extends CamelCaseJsonProtocol {

  def repository: ShippearRepository[T]

  def create(doc: T) = repository.create(doc)

  def findBy(params: Map[String, String]) = repository.findBy(params)

  def update(doc: T) = repository.update(doc)

  def all = repository.all
}
