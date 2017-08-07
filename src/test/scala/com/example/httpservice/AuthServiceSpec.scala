package com.example.httpservice

import com.example.model.User
import io.circe.Json
import io.circe.parser.parse
import org.http4s.{Cookie, Header, HttpService}
import org.http4s.util.CaseInsensitiveString
import org.scalatest.Matchers._

class AuthServiceSpec extends HttpTestSuite with testUserDao {

  val authHttpService: HttpService = AuthHttpService(userDaoTest).service

  test("log user") {
    val user = User("petro1", "petro1@mail.com", "123456")
    userDaoTest.insert(user)

    val setCookieHeader = obtainAuthSetCookie(user.email, user.pass)

    assert(setCookieHeader.isDefined)
    assert(setCookieHeader.get.contains("authcookie"))
  }

  test("use a logged endpoint") {
    val user = User("petro2", "petro2@mail.com", "123456")
    userDaoTest.insert(user)
    val setCookieHeader = obtainAuthSetCookie(user.email, user.pass)
    val headerName = setCookieHeader.get.substring(0, setCookieHeader.get.indexOf("="))
    val headerValue = setCookieHeader.get.substring(setCookieHeader.get.indexOf("=") + 1)
    val authHeader = Header("Cookie", Cookie(headerName, headerValue).renderString)

    val response = get(authHttpService, "/verifyLogged", List(authHeader))

    assert(response.status.code == 200)
    val body = getResponseBody(response)
    assert(body == "Welcome, you are logged " + user.name)
  }

  def obtainAuthSetCookie(email: String, pass: String): Option[String] = {
    val bodyRequest = parse("{\"email\":\"" + email + "\", \"password\":\"" + pass + "\"}").right.get
    val response = post(authHttpService, "/login", bodyRequest)
    assert(response.status.code == 200)
    val headers = getResponseHeaders(response)
    headers.get(CaseInsensitiveString("Set-Cookie")).map(_.value)
  }

}
