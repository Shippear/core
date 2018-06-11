package service

import repository.ShippearRepository

trait Service[T] {

  def repository: ShippearRepository[T]

  def create(doc: T) = repository.create(doc)

  def findBy(params: Map[String, String]) = repository.findBy(params)

  def update(doc: T) = repository.update(doc)

  def all = repository.all
}
