package com.example.httpservice

import io.circe.Json
import org.http4s._
import org.scalatest.FunSuite
import org.http4s._
import org.http4s.circe._

/**
  * Created by petro on 15/6/2017.
  */
class CrudTestSuite extends FunSuite {

  private def buildRequest(method: Method, uri: String, bodyOpt: Option[Json] = None): Request = {
    val uriString = Uri.fromString(uri)
    assert(uriString.isRight)
    val request = Request(method, uriString.right.get)
    bodyOpt match {
      case Some(body) => request.withBody(body).unsafeRun()
      case None => request
    }
  }

  private def makeRequest(service: HttpService, method: Method, uri: String, bodyOpt: Option[Json] = None): Response = {
    val request = buildRequest(method, uri, bodyOpt)
    val task = service.run(request)
    val response = task.unsafeRun.toOption
    assert(response.isDefined)
    response.get
  }

  def get(service: HttpService, uri: String): Response = makeRequest(service, Method.GET, uri)

  def post(service: HttpService, uri: String, body: Json): Response = makeRequest(service, Method.POST, uri, Some(body))

  def put(service: HttpService, uri: String, body: Json): Response = makeRequest(service, Method.PUT, uri, Some(body))

  def delete(service: HttpService, uri: String): Response = makeRequest(service, Method.DELETE, uri)

  def getResponseBody(response: Response): String =
    response.orNotFound.bodyAsText.runLog.unsafeRun.head

}
