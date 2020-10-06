package ru.infobis.zio.reports.http

import io.circe.Encoder
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import zio._
import zio.interop.catz._
import ru.infobis.zio.reports.Report
import ru.infobis.zio.reports.repository.ReportsRepository

object ReportService {

  def routes[R <: ReportsRepository](): HttpRoutes[RIO[R, ?]] = {
    type ReportTask[A] = RIO[R, A]

    val dsl: Http4sDsl[ReportTask] = Http4sDsl[ReportTask]
    import dsl._

    implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[ReportTask, A] = jsonEncoderOf[ReportTask, A]

    HttpRoutes.of[ReportTask] {
      case GET -> Root / LongVar(id) =>
        getReportById(id).flatMap {
          _.fold(NotFound())(x => Ok(x))
        }


      case GET -> Root / "interrupt" / LongVar(startTimeMillis) / LongVar(seqNumber) =>
        interruptReportByFiberId(startTimeMillis, seqNumber).flatMap {
          _ => Ok(s"fiber id=${Fiber.Id(startTimeMillis, seqNumber)} interrupted.")
        }


    }

  }

  def interruptReportByFiberId[R <: ReportsRepository](startTimeMillis: Long, seqNumber: Long): UIO[Nothing] = {
    val fiberid = Fiber.Id(startTimeMillis, seqNumber)
    ZIO.interruptAs(fiberid)
  }


  def getReportById[R <: ReportsRepository](id: Long): URIO[ReportsRepository, Option[Report]] = {
    for {
      fiber <- ReportsRepository.getById(id).fork
      report <- {
        println(s"id=${id} fiber.id= ${fiber.id}")
        fiber.join
      }
    } yield report
  }

}
