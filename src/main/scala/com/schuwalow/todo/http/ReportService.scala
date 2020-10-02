package com.schuwalow.todo.http

import com.schuwalow.todo.Report
import io.circe.Encoder
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import zio._
import zio.interop.catz._
import com.schuwalow.todo.repository._

object ReportService {

  def routes[R <: ReportsRepository](): HttpRoutes[RIO[R, ?]] = {
    type ReportTask[A] = RIO[R, A]

    val dsl: Http4sDsl[ReportTask] = Http4sDsl[ReportTask]
    import dsl._

    implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[ReportTask, A] = jsonEncoderOf[ReportTask, A]

    HttpRoutes.of[ReportTask] {
      case GET -> Root / LongVar(id) =>
        for {
          report <- ReportsRepository.getById(id)
          response <- report.fold(NotFound())((x: Report) => Ok( x))
        } yield response
    }

  }
}
