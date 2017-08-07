package com.example.httpservice

import io.circe.Json
import org.http4s._
import org.http4s.circe._
import org.scalatest.FunSuite

/**
  * Created by petro on 15/6/2017.
  */
trait HttpTestSuite extends FunSuite {

  private def buildRequest(method: Method, uri: String, bodyOpt: Option[Json] = None,
                           headers: List[Header]): Request = {
    val uriString = Uri.fromString(uri)
    assert(uriString.isRight)
    val request = Request(method, uriString.right.get)
    val requestWithHeaders = request.replaceAllHeaders(Headers(headers))
    bodyOpt match {
      case Some(body) => requestWithHeaders.withBody(body).unsafeRun()
      case None => requestWithHeaders
    }
  }

  private def makeRequest(service: HttpService, method: Method, uri: String,
                          headers: List[Header], bodyOpt: Option[Json] = None): Response = {
    val request = buildRequest(method, uri, bodyOpt, headers)
    val task = service.run(request)
    val response = task.unsafeRun.toOption
    assert(response.isDefined)
    response.get
  }

  def get(service: HttpService, uri: String, headers: List[Header] = List()): Response =
    makeRequest(service, Method.GET, uri, headers)

  def post(service: HttpService, uri: String, body: Json, headers: List[Header] = List()): Response =
    makeRequest(service, Method.POST, uri, headers, Some(body))

  def put(service: HttpService, uri: String, body: Json, headers: List[Header] = List()): Response =
    makeRequest(service, Method.PUT, uri, headers, Some(body))

  def delete(service: HttpService, uri: String, headers: List[Header] = List()): Response =
    makeRequest(service, Method.DELETE, uri, headers)

  def getResponseBody(response: Response): String =
    response.orNotFound.bodyAsText.runLog.unsafeRun.head

  def getResponseHeaders(response: Response): Headers =
    response.orNotFound.headers

}
