import controllers.ReportService
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze._
import repository.InMemoryReportRepository
import zio.{ZEnv, _}
import zio.interop.catz._
import zio.interop.catz.implicits._

object Starter extends App {

  val baseUrl = "http://localhost:8080"


  val server: ZIO[ZEnv, Throwable, Unit] = ZIO.runtime[ZEnv]
    .flatMap {
      implicit rts =>
        BlazeServerBuilder
          .apply[Task](rts.platform.executor.asEC)
          .bindHttp(8081, "localhost")
          .withHttpApp {
            Router[Task](
              "/reports" -> ReportService.routes(s"${baseUrl}/reports")
            ).orNotFound
          }
          .serve
          .compile
          .drain
    }

  def run(args: List[String]) =
    server.fold(_ => zio.ExitCode(1), _ => zio.ExitCode(0)).provideSomeLayer[ZEnv](InMemoryReportRepository.inMemoryLayer)
}




