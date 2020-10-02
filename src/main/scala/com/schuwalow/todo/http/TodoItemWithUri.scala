package com.schuwalow.todo.http

import io.circe.generic.semiauto._
import io.circe.{ Decoder, Encoder }

import com.schuwalow.todo._

final case class TodoItemWithUri(
  id: Long,
  url: String,
  title: String,
  completed: Boolean,
  order: Option[Int])

object TodoItemWithUri {

  def apply(
    basePath: String,
    todoItem: Report
  ): TodoItemWithUri =
    TodoItemWithUri(
      todoItem.id,
      s"$basePath/${todoItem.id}",
      todoItem.item.title,
      todoItem.item.completed,
      todoItem.item.order
    )

  implicit val encoder: Encoder[TodoItemWithUri] = deriveEncoder
  implicit val decoder: Decoder[TodoItemWithUri] = deriveDecoder
}
