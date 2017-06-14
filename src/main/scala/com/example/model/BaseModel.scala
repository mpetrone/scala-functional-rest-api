package com.example.model

import java.time.LocalDateTime
import java.util.UUID

trait BaseModel extends Object{
  val id: UUID
  val deleted: Boolean
  val created: LocalDateTime
  val modified: LocalDateTime
}

