package service

import repository.ShippearRepository

trait Service[T] {

  def repository: ShippearRepository[T]

  def create(doc: T) = repository.create(doc)

  def findBy(params: Map[String, String]) = repository.findBy(params)

  def findOneBy(params: Map[String, String]) = repository.findOneBy(params)

  def findById(id: String) = repository.findOneById(id)

  def replace(doc: T) = repository.replace(doc)

  def update(doc: T) = repository.update(doc)

  def all = repository.all
}
