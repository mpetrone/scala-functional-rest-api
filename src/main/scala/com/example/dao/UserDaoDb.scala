package com.example.dao

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID

import cats.implicits._
import com.example.model.User
import doobie.imports._
import fs2.Task
import fs2.interop.cats._

case class UserDaoDb(xa: Transactor[Task]) extends UserDao {

  implicit val DateTimeMeta: Meta[LocalDateTime] =
    Meta[java.sql.Timestamp].nxmap(
      timeStamp => timeStamp.toLocalDateTime,
      localDateTime => Timestamp.valueOf(localDateTime)
    )

  implicit val uuidMeta: Meta[UUID] =
    Meta[String].nxmap(UUID.fromString, _.toString)

  val createTableQuery: Update0 = sql"""
   CREATE TABLE users (
       id character(128) NOT NULL PRIMARY KEY,
       name character varying(255) NOT NULL,
       email character varying(128),
       pass character varying(128),
       deleted boolean NOT NULL,
       created timestamp without time zone NOT NULL,
       modified timestamp without time zone NOT NULL
   );
  """.update

  val dropTableQuery: Update0 = sql"DROP TABLE IF EXISTS clients CASCADE;".update

  val getAllQuery: Query0[User] = sql"select * from clients".query[User]

  def getQuery(id: String): Query0[User] = sql"select * from clients where id = $id".query[User]

  def getByEmailQuery(email: String): Query0[User] = sql"select * from clients where email = $email".query[User]

  def insertQuery(user: User): Update0 =
    sql"""insert into clients (id, name, email, pass, deleted, modified, created)
          values (${user.id}, ${user.name}, ${user.email}, ${user.pass},
                  ${user.deleted}, ${user.modified}, ${user.created})""".update

  def updateQuery(user: User): Update0 =
    sql"""update clients set (name, email, pass, deleted, modified, created) =
           (${user.name}, ${user.pass}, ${user.deleted}, ${user.modified}, ${user.created})
          where id = ${user.id}""".update

  def buildSchema: Task[Int] = (dropTableQuery.run *> createTableQuery.run).transact(xa)

  override def getAll: Task[List[User]] =
    getAllQuery
      .list
      .transact(xa)

  override def get(id: String): Task[Option[User]] = getQuery(id)
    .option
    .transact(xa)

  override def getByEmail(email: String): Task[Option[User]] = getByEmailQuery(email)
    .option
    .transact(xa)

  override def insert(user: User): Task[String] =
    insertQuery(user)
      .withUniqueGeneratedKeys[String]("id")
      .transact(xa)

  override def update(user: User): Task[String] =
    updateQuery(user).withUniqueGeneratedKeys[String]("id").transact(xa)

  override def delete(id: String): Task[Option[String]] =
    get(id).flatMap {
      case Some(client) => update(client.delete).map(Some(_))
      case None => Task.delay(None)
    }
}
