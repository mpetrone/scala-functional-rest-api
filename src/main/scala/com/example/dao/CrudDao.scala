package com.example.dao

import fs2.Task

import scala.language.higherKinds

trait CrudDao[T] {

  def getAll: Task[List[T]]

  def get(id: String): Task[Option[T]]

  def insert(entityToInsert: T): Task[String]

  def update(entityToUpdate: T): Task[String]

  def delete(id: String): Task[Option[String]]
}

