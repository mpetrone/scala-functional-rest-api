package com.example.httpservice

import com.example.dao.CrudDao
import com.example.model.User
import io.circe.parser.{decode, _}
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.scalatest.Matchers._

class UserServiceSpec extends HttpTestSuite with testUserDao {

  implicit val userEntityEncoder: EntityEncoder[User] = jsonEncoderOf[User]

  lazy val userHttpService: HttpService = UserHttpService(userDaoTest).service

  test("get clients") {
    val user1 = User("petro1", "petro1@mail.com", "123456")
    val user2 = User("petro2", "petro2@mail.com", "123456")
    val user3 = User("petro3", "petro3@mail.com", "123456")
    users = Map(user1.id.toString -> user1, user2.id.toString -> user2, user3.id.toString -> user3)

    val response = get(userHttpService, "/users")

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

    val response = get(userHttpService, s"/users/${user.id.toString}")

    assert(response.status.code == 200)
    val body = getResponseBody(response)
    val parsedResponse = decode[User](body)
    assert(parsedResponse.isRight)
    assert(user == parsedResponse.right.get)
  }

  test("get invalid client") {
    val response = get(userHttpService, "/users/invalid-id")
    assert(response.status.code == 404)
  }

  test("insert client") {
    val user = User("petro", "petro@mail.com", "123456")

    val response = post(userHttpService, "/users", user.asJson)

    assert(response.status.code == 200)
    users.get(user.id.toString) should be (Some(user))
  }

  test("insert invalid client") {
    val invalidJson = parse("""{"dummy":"test"}""").right.get

    val response = post(userHttpService, "/users", invalidJson)

    assert(response.status.code == 400)
  }

  test("delete client") {
    val user = User("petro", "petro@mail.com", "123456")
    users = Map(user.id.toString -> user)

    val response = delete(userHttpService, s"/users/${user.id.toString}")

    assert(response.status.code == 200)
    users.get(user.id.toString).map(_.deleted) should be (Some(true))
  }


}
