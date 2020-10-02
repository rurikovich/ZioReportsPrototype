package com.schuwalow.todo.http

import io.circe.generic.semiauto._
import io.circe.{ Decoder, Encoder }

import com.schuwalow.todo._

final case class TodoItemWithUri(
  id: Long,
  url: String,
  body: String)

object TodoItemWithUri {

  def apply(
    basePath: String,
    todoItem: Report
  ): TodoItemWithUri =
    TodoItemWithUri(
      todoItem.id,
      s"$basePath/${todoItem.id}",
      todoItem.body
    )

  implicit val encoder: Encoder[TodoItemWithUri] = deriveEncoder
  implicit val decoder: Decoder[TodoItemWithUri] = deriveDecoder
}
