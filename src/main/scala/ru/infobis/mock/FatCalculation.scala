package ru.infobis.mock

import java.time.LocalDateTime
import java.time.LocalDateTime.now

import ru.infobis.zio.reports.Report

trait FatCalculation {

  val requestDurationInSeconds = 600L

  def veryLongAndFatReportById(id: Long, secondsToCalculateReport: Long): Option[Report] = {
    val startTime: LocalDateTime = now()
    val endTime = startTime.plusSeconds(secondsToCalculateReport)

    var veryFatVar = (0 to 1000).foldLeft("")((res, i) => res + s"i=${i}_")

    var i = 0
    while ((now() isBefore endTime) && !Thread.interrupted()) {
      veryFatVar = veryFatVar + s"square=${Math.sqrt(111_231)}"
      if (now().getSecond % 10 == 0) {
        println(s"veryFatVar $i")
      }
      i += 1
    }

    Some(Report(id, veryFatVar))
  }

}
