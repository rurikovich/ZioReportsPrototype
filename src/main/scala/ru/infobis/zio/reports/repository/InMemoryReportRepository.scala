package ru.infobis.zio.reports.repository

import java.time.LocalDateTime._

import ru.infobis.zio.reports._
import zio._

final private class InMemoryReportRepository()
  extends ReportsRepository.Service {

  val requestDurationInSeconds = 60L

  override def getById(id: Long): UIO[Option[Report]] = UIO.succeed(veryLongAndFatReportById(id, requestDurationInSeconds))

  def veryLongAndFatReportById(id: Long, secondsToCalculateReport: Long): Option[Report] = {
    val startTime = now()
    val endTime = startTime.plusSeconds(secondsToCalculateReport)

    var veryFatVar = (0 to 1000).foldLeft("")((res, i) => res + s"i=${i}_")
    while (now() isBefore endTime) {
      veryFatVar = veryFatVar + s"square=${Math.sqrt(111_231)}"
      println(s"veryFatVar ")
    }

    Some(Report(id, veryFatVar))
  }

}

object InMemoryReportRepository {

  val layer: ZLayer[Any, Nothing, ReportsRepository] =
    ZLayer.succeed {
      new InMemoryReportRepository()
    }
}
