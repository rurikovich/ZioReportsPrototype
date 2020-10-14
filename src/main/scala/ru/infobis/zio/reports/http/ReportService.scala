package ru.infobis.zio.reports.http

import io.circe.Encoder
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import ru.infobis.mock.FatCalculation
import zio._
import zio.interop.catz._
import ru.infobis.zio.reports.Report
import ru.infobis.zio.reports.fibers.FiberManager
import ru.infobis.zio.reports.repository.ReportsRepository
import zio.blocking._

object ReportService extends FatCalculation {

  type ReportServiceType = ReportsRepository with FiberManager with Blocking

  def routes[R <: ReportServiceType](): HttpRoutes[RIO[R, ?]] = {
    type ReportTask[A] = RIO[R, A]

    val dsl: Http4sDsl[ReportTask] = Http4sDsl[ReportTask]
    import dsl._

    implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[ReportTask, A] = jsonEncoderOf[ReportTask, A]

    HttpRoutes.of[ReportTask] {
      case GET -> Root / LongVar(id) =>
        for {
          report <- getReportById(id).flatMap {
            _.fold(NotFound())(x => Ok(x))
          }.fork

          res<-report.join
        } yield res


      case GET -> Root / "interrupt" / UUIDVar(uuid) =>
        FiberManager.interruptFiber(uuid).flatMap {
          _ => Ok(s"fiber uuid=$uuid interrupted.")
        }

      case GET -> Root / "fibers" =>
        FiberManager.listFibers().flatMap(list => Ok(list))


    }

  }

  def interruptReportByFiberId[R <: ReportServiceType](startTimeMillis: Long, seqNumber: Long): UIO[Nothing] = {
    val fiberid = Fiber.Id(startTimeMillis, seqNumber)
    ZIO.interruptAs(fiberid)
  }


  def blockingEffect(id:Long): RIO[Blocking, Option[Report]] = effectBlockingInterrupt {
    veryLongAndFatReportById(id, requestDurationInSeconds)
  }


  def getReportById[R <: ReportServiceType](id: Long): RIO[ReportServiceType, Option[Report]] = {

    for {
      fiber: Fiber.Runtime[Throwable, Option[Report]] <-  ReportsRepository.getById(id).fork
      uuid <- FiberManager.addFiber(fiber)
      report <- {
        println(s"id=$id fiber.id= ${fiber.id} ")
        fiber.join
      }
      _ <-FiberManager.removeFiber(uuid)
    } yield report
  }

}
