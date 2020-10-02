package com.schuwalow.todo.http

import io.circe.generic.semiauto._
import io.circe.{ Decoder, Encoder }

import com.schuwalow.todo._

final case class ReportWithUri(
  id: Long,
  url: String,
  body: String)

object ReportWithUri {

  def apply(
    basePath: String,
    todoItem: Report
  ): ReportWithUri =
    ReportWithUri(
      todoItem.id,
      s"$basePath/${todoItem.id}",
      todoItem.body
    )

  implicit val encoder: Encoder[ReportWithUri] = deriveEncoder
  implicit val decoder: Decoder[ReportWithUri] = deriveDecoder
}
