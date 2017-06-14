package com.example.httpservice

import cats.implicits._
import com.example.dao.UserDao
import com.example.model.User
import io.circe.syntax._
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl._

case class UserHttpService(dao: UserDao) extends CrudHttpService[User] {

  val service: HttpService = userService |+| crudService("clients")

  lazy val userService: HttpService = buildHttpService {
    case POST -> Root / "users" / "initDB" =>
      Ok(dao.buildSchema.map(_.asJson))
    case GET -> Root / "users" / "hello" =>
      Ok("world")
  }
}


