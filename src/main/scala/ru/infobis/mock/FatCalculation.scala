package ru.infobis.mock

import java.time.LocalDateTime
import java.time.LocalDateTime.now

import ru.infobis.zio.reports.Report

trait FatCalculation {

  val requestDurationInSeconds = 60L

  def veryLongAndFatReportById(id: Long, secondsToCalculateReport: Long): Option[Report] = {
    val startTime: LocalDateTime = now()
    val endTime = startTime.plusSeconds(secondsToCalculateReport)

    var veryFatVar = (0 to 1000).foldLeft("")((res, i) => res + s"i=${i}_")

    var i = 0
    while (now() isBefore endTime) {
      Thread.sleep(1000)
      veryFatVar = veryFatVar + s"square=${Math.sqrt(111_231)}"
      println(s"veryFatVar $i")
      i += 1
    }

    Some(Report(id, veryFatVar))
  }

}