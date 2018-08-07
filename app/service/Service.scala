package service

import repository.ShippearRepository

trait Service[T] {

  def repository: ShippearRepository[T]

  def create(doc: T) = repository.create(doc)

  def findBy(params: Map[String, String]) = repository.findBy(params)

  def findById(id: String) = repository.findOneById(id)

  def update(doc: T) = repository.replace(doc)

  def all = repository.all
}
