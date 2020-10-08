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

object ReportService  extends FatCalculation{

  def routes[R <: ReportsRepository with Blocking](): HttpRoutes[RIO[R, ?]] = {
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


  def getReportById[R <: ReportsRepository with Blocking](id: Long): RIO[ReportsRepository with Blocking, Option[Report]] = {

    for {
      fiber <- blockingEffect.fork
      timerFiber <- timer.fork
      valid <- timerFiber.join
      _ <- if (!valid) fiber.interrupt.fork else IO.unit

      report <- {
        println(s"id=$id fiber.id= ${fiber.id} $valid")
        fiber.join
      }
    } yield report
  }

}
