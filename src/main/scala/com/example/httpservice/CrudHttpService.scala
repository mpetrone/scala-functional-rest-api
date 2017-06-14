package com.example.httpservice

import com.example.dao.CrudDao
import fs2.Task
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.server.middleware._

trait CrudHttpService[T]{

  def dao: CrudDao[T]

  def crudService(uri: String = "")(implicit encoder: Encoder[T], decoder: Decoder[T]): HttpService = buildHttpService {
    case GET -> Root / `uri`  =>
      Ok(dao.getAll.map(_.asJson))

    case req @ GET -> Root / `uri` / id =>
      dao.get(id).flatMap{
        case Some(entity) => Ok(entity.asJson)
        case None => Response.notFound(req)
      }

    case req @ POST -> Root / `uri`  =>
      for {
        entity <- req.as(jsonOf[T])
        id <- dao.insert(entity)
        resp <- Ok(id)
      } yield resp

    case req @ PUT -> Root / `uri`  =>
      for {
        entity <- req.as(jsonOf[T])
        id <- dao.update(entity)
        resp <- Ok(id)
      } yield resp

    case req @ DELETE -> Root / `uri` / id =>
      dao.delete(id).flatMap {
        case Some(_) => Ok(id)
        case None => Response.notFound(req)
      }
  }

  def buildHttpService(pf: PartialFunction[Request, Task[Response]]): HttpService =
    CORS(HttpService(pf.andThen(_.handleWith(errorHandler))))

  private val errorHandler: PartialFunction[Throwable, Task[Response]] = {
    case MalformedMessageBodyFailure(details, _)      => BadRequest(details)
    case InvalidMessageBodyFailure(details, _)        => BadRequest(details)
  }
}