package com.example.model

import java.time.LocalDateTime
import java.util.UUID

import io.circe.generic.JsonCodec
import io.circe.java8.time._

@JsonCodec case class User (
   id: UUID = UUID.randomUUID,
   name: String,
   email: String,
   pass: String,
   deleted: Boolean = false,
   created: LocalDateTime = LocalDateTime.now(),
   modified: LocalDateTime = LocalDateTime.now()
 )  extends BaseModel {
  def delete: User = copy(deleted = true)
}

object User {
  def apply(
             name: String,
             email: String,
             pass: String
           ): User = new User(name = name, email = email, pass = pass)
}