package ru.infobis.zio.reports

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

final case class Report(id: Long, body: String)

object Report {
  implicit val encoder: Encoder[Report] = deriveEncoder
  implicit val decoder: Decoder[Report] = deriveDecoder
}

