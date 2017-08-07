package com.example.dao
import com.example.model.User
import fs2.Task

trait UserDao extends CrudDao[User] {

  def buildSchema: Task[Int]

  def getAll: Task[List[User]]

  def get(id: String): Task[Option[User]]

  def getByEmail(email: String): Task[Option[User]]

  def insert(user: User): Task[String]

  def update(user: User): Task[String]

  def delete(id: String): Task[Option[String]]
}
