package com.example.main

import com.example.dao.UserDaoDb
import com.example.httpservice.UserHttpService
import doobie.imports.{DriverManagerTransactor, Transactor}
import fs2.{Stream, Task}
import org.http4s.server.blaze._
import org.http4s.util.StreamApp

object Main extends StreamApp {
  override def stream(args: List[String]): Stream[Task, Nothing] = {

    val userDao = UserDaoDb(DBConnection.xa)
    val userHttpService = UserHttpService(userDao)

    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(userHttpService.service, "/api")
      .serve
  }
}

object DBConnection {
  val driver = "org.postgresql.Driver"
  val connectionString = "jdbc:postgresql:example"
  val user = "postgres"
  val pass = "postgres"

  lazy val xa: Transactor[Task] = DriverManagerTransactor[Task](driver, connectionString, user, pass)
}

