package com.example.httpservice

import java.time.Clock

import cats.data._
import cats.implicits._
import com.example.dao.UserDao
import com.example.model.User
import fs2.Task
import io.circe.generic.JsonCodec
import org.http4s.circe.jsonOf
import org.http4s.dsl.{->, /, POST, Root, _}
import org.http4s.server._
import org.http4s.{HttpService, Response, Status, _}
import org.reactormonk.{CryptoBits, PrivateKey}

case class AuthHttpService(userDao: UserDao) {

  val service: HttpService = authService |+| middleware(authedService)
  val key = PrivateKey(scala.io.Codec.toUTF8(scala.util.Random.alphanumeric.take(20).mkString("")))
  val crypto = CryptoBits(key)
  val clock: Clock = Clock.systemUTC
  @JsonCodec case class LoginRequest(email: String, password: String)

  lazy val authService: HttpService = HttpService {
    case POST -> Root / "signup" =>
      Task.delay(Response(Status.Ok))
    case req @ POST -> Root / "login" =>
      verifyLogin(req).flatMap({
        case Left(error) =>
          Forbidden(error)
        case Right(user) =>
          val message = crypto.signToken(user.id.toString, clock.millis.toString)
          Ok("Logged in!").addCookie(Cookie("authcookie", message))
      })
  }

  lazy val authedService: AuthedService[User] =
    AuthedService {
      case GET -> Root / "verifyLogged" as user => Ok(s"Welcome, you are logged ${user.name}")
    }

  def verifyLogin(request: Request): Task[Either[String,User]] = {
    for {
      loginRequest <- request.as(jsonOf[LoginRequest])
      userOpt <- userDao.getByEmail(loginRequest.email)
    } yield userOpt.toRight("user not found")
  }

  def retrieveUser(userId: String): Task[Either[String, User]] =
    userDao.get(userId).map(_.toRight("user not found"))

  lazy val authUser: Service[Request, Either[String, User]] = Kleisli({ request =>
    println(request.headers.toString())
    val userIdE = for {
      header <- headers.Cookie.from(request.headers).toRight("Cookie parsing error")
      cookie <- header.values.toList.find(_.name == "authcookie").toRight("Couldn't find the authcookie")
      userId <- crypto.validateSignedToken(cookie.content).toRight("Cookie invalid")
    } yield userId
    Task.now(userIdE).flatMap {
      case Right(userId) => retrieveUser(userId)
      case Left(error) =>  Task.now(Left(error))
    }
  })

  lazy val onFailure: AuthedService[String] = Kleisli(req => Forbidden(req.authInfo))
  lazy val middleware = AuthMiddleware(authUser, onFailure)
}
