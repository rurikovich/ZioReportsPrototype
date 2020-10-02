package ru.infobis.zio.reports.http

import io.circe.generic.semiauto._
import io.circe.{ Decoder, Encoder }

import ru.infobis.zio.reports._

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
