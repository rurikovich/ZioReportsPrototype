package com.schuwalow.todo.http

import HTTPSpec._
import io.circe.literal._
import org.http4s.circe._
import org.http4s.implicits._
import org.http4s.{Status, _}
import zio._
import zio.interop.catz._
import zio.test._
import com.schuwalow.todo.repository.{InMemoryReportRepository, ReportsRepository}

object ReportServiceSpec extends DefaultRunnableSpec {
  type ReportTask[A] = RIO[ReportsRepository, A]

  val app = ReportService.routes[ReportsRepository]().orNotFound

  override def spec =
    suite("ReportService")(

      testM("should show report by id") {
        val req = request[ReportTask](Method.GET, "/10000")
        checkRequest(
          app.run(req),
          Status.Ok,
          Some(json"""{"id":10000,"body":"eeee"}""")
        )
      }

    ).provideSomeLayer[ZEnv](InMemoryReportRepository.layer)
}
