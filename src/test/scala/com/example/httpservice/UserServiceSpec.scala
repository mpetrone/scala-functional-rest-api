package com.example.httpservice

import com.example.dao.CrudDao
import com.example.model.User
import fs2.Task
import io.circe.Json
import io.circe.parser.{decode, _}
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.scalatest.Matchers._
import org.scalatest._

class UserServiceSpec extends FunSuite {

  implicit val userEntityEncoder: EntityEncoder[User] = jsonEncoderOf[User]
  var users = Map.empty[String, User]

  lazy val testDAO = new CrudDao[User] {
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

  lazy val userHttpService: HttpService = new CrudHttpService[User] {
    val dao: CrudDao[User] = testDAO
  }.crudService("users")

  test("get clients") {
    val user1 = User("petro1", "petro1@mail.com", "123456")
    val user2 = User("petro2", "petro2@mail.com", "123456")
    val user3 = User("petro3", "petro3@mail.com", "123456")
    users = Map(user1.id.toString -> user1, user2.id.toString -> user2, user3.id.toString -> user3)
    val request = buildRequest(Method.GET, "/users")
    val task = userHttpService.run(request)
    val response = task.unsafeRun.orNotFound
    assert(response.status.code == 200)
    val body = getResponseBody(response)
    val parsedResponse = decode[List[User]](body)
    assert(parsedResponse.isRight)
    assert(parsedResponse.right.get.length == 3)
    assert(user1 == parsedResponse.right.get.head)
    assert(user2 == parsedResponse.right.get(1))
    assert(user3 == parsedResponse.right.get(2))
  }

  test("get a client") {
    val user = User("petro", "petro@mail.com", "123456")
    users = Map(user.id.toString -> user)
    val request = buildRequest(Method.GET, s"/users/${user.id.toString}")
    val task = userHttpService.run(request)
    val response = task.unsafeRun.orNotFound
    assert(response.status.code == 200)
    val body = getResponseBody(response)
    val parsedResponse = decode[User](body)
    assert(parsedResponse.isRight)
    assert(user == parsedResponse.right.get)
  }

  test("get invalid client") {
    val request = buildRequest(Method.GET, "/users/invalid-id")
    val task = userHttpService.run(request)
    val response = task.unsafeRun.orNotFound
    assert(response.status.code == 404)
  }

  test("insert client") {
    val user = User("petro", "petro@mail.com", "123456")
    val request = buildRequest(Method.POST, "/users", Some(user.asJson))
    val task = userHttpService.run(request)
    val response = task.unsafeRun.orNotFound
    assert(response.status.code == 200)
    users.get(user.id.toString) should be (Some(user))
  }

  test("insert invalid client") {
    val invalidJson = parse("""{"dummy":"test"}""").right.get
    val request = buildRequest(Method.POST, "/users", Some(invalidJson))
    val task = userHttpService.run(request)
    val response = task.unsafeRun.orNotFound
    assert(response.status.code == 400)
  }

  test("delete client") {
    val user = User("petro", "petro@mail.com", "123456")
    users = Map(user.id.toString -> user)
    val request = buildRequest(Method.DELETE, s"/users/${user.id.toString}")
    val task = userHttpService.run(request)
    val response = task.unsafeRun.orNotFound
    assert(response.status.code == 200)
    users.get(user.id.toString).map(_.deleted) should be (Some(true))
  }

  private def buildRequest(method: Method, uri: String, bodyOpt: Option[Json] = None): Request = {
    val uriString = Uri.fromString(uri)
    assert(uriString.isRight)
    val request = Request(method, uriString.right.get)
    bodyOpt match {
      case Some(body) => request.withBody(body).unsafeRun()
      case None => request
    }
  }

  private def getResponseBody(response: Response): String =
    response.orNotFound.bodyAsText.runLog.unsafeRun.head
}
