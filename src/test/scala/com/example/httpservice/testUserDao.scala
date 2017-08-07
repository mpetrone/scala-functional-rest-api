package com.example.httpservice

import com.example.dao.{CrudDao, UserDao, UserDaoDb}
import com.example.model.User
import fs2.Task

/**
  * Created by petro on 28/7/2017.
  */
trait testUserDao {

  var users = Map.empty[String, User]

  val userDaoTest = new UserDao {
    def buildSchema: Task[Int] = ???
    def getAll: Task[List[User]] = Task.now(users.values.toList)
    def get(id: String): Task[Option[User]] = Task.now(users.get(id))
    override def getByEmail(email: String): Task[Option[User]] = {
      Task.now(users.values.toList.find(_.email.equals(email)))
    }
    def insert(user: User): Task[String] = Task.now {
      users = users + (user.id.toString -> user)
      user.id.toString
    }
    def update(user: User): Task[String] = insert(user)
    def delete(id: String): Task[Option[String]] = {
      users.get(id) match {
        case Some(user) =>
          users = users + (user.id.toString -> user.delete)
          Task.now(Some(user.id.toString))
        case None => Task.now(None)
      }
    }
  }

  lazy val crudDAO = new CrudDao[User] {
    def getAll: Task[List[User]] = Task.now(users.values.toList)
    def get(id: String): Task[Option[User]] = Task.now(users.get(id))
    def insert(user: User): Task[String] = Task.now {
      users = users + (user.id.toString -> user)
      user.id.toString
    }
    def update(user: User): Task[String] = insert(user)
    def delete(id: String): Task[Option[String]] = {
      users.get(id) match {
        case Some(user) =>
          users = users + (user.id.toString -> user.delete)
          Task.now(Some(user.id.toString))
        case None => Task.now(None)
      }
    }
  }

}
