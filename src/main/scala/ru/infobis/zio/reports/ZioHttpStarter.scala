package ru.infobis.zio.reports

import cats.effect._
import fs2.Stream.Compiler._
import org.http4s.HttpApp
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio.clock.Clock
import zio.interop.catz._
import zio.{ExitCode => ZExitCode, _}
import ru.infobis.zio.reports.config._

import ru.infobis.zio.reports.http.ReportService

object ZioHttpStarter extends App {
  type AppTask[A] = RIO[layers.AppEnv with Clock, A]

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ZExitCode] = {
    val prog =
      for {
        cfg <- getAppConfig
        _ <- logging.log.info(s"Starting with $cfg")
        httpApp = Router[AppTask](
          "/reports" -> ReportService.routes()
        ).orNotFound

        _ <- runHttp(httpApp, cfg.http.port)
      } yield ZExitCode.success

    prog.provideSomeLayer[ZEnv](layers.live.appLayer).orDie
  }

  def runHttp[R <: Clock](httpApp: HttpApp[RIO[R, *]], port: Int): ZIO[R, Throwable, Unit] = {
    type Task[A] = RIO[R, A]
    ZIO.runtime[R].flatMap {
      implicit rts: Runtime[R] =>
        BlazeServerBuilder
          .apply[Task](rts.platform.executor.asEC)
          .bindHttp(port, "0.0.0.0")
          .withHttpApp(CORS(httpApp))
          .serve
          .compile[Task, Task, ExitCode]
          .drain
    }
  }

}
