package ru.infobis.zio.reports.http

import io.circe.Encoder
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import ru.infobis.mock.FatCalculation
import zio._
import zio.interop.catz._
import ru.infobis.zio.reports.Report
import ru.infobis.zio.reports.repository.ReportsRepository
import zio.blocking._
import zio.clock.Clock

object ReportService  extends FatCalculation{

  type ReportServiceType = ReportsRepository with Blocking with Clock

  def routes[R <: ReportServiceType](): HttpRoutes[RIO[R, ?]] = {
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



  val blockingEffect: RIO[Blocking, Option[Report]] = effectBlockingInterrupt {
    veryLongAndFatReportById(1,requestDurationInSeconds)
  }

  val timer: RIO[Blocking, Boolean] = effectBlocking {
    Thread.sleep(10_000)
    false
  }


  def getReportById[R <: ReportServiceType](id: Long): RIO[ReportServiceType, Option[Report]] = {
    import zio.duration._
    println(id)
    for {
      report <- blockingEffect.timeout(10.seconds)
    } yield report.flatten
  }

}
