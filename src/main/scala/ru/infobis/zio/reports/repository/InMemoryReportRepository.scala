package ru.infobis.zio.reports.repository

import java.time.LocalDateTime._

import ru.infobis.zio.reports._
import zio._

final private class InMemoryReportRepository()
  extends ReportsRepository.Service {

  val requestDurationInSeconds = 20L

  override def getById(id: Long): UIO[Option[Report]] = UIO.succeed(veryLongAndFatReportById(id, requestDurationInSeconds))

  def veryLongAndFatReportById(id: Long, secondsToCalculateReport: Long): Option[Report] = {
    val startTime = now()
    val endTime = startTime.plusSeconds(secondsToCalculateReport)

    var veryFatVal = (0 to 1000).foldLeft("")((res, i) => res + s"i=${i}_")
    while (now() isBefore endTime) {
      veryFatVal = veryFatVal + s"square=${Math.sqrt(111_231)}"
    }

    Some(Report(id, veryFatVal))
  }

}

object InMemoryReportRepository {

  val layer: ZLayer[Any, Nothing, ReportsRepository] =
    ZLayer.succeed {
      new InMemoryReportRepository()
    }
}
