import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.blaze._
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._

object Hello1 extends App {

  val server: ZIO[ZEnv, Throwable, Unit] = ZIO.runtime[ZEnv]
    .flatMap {
      implicit rts =>
        BlazeServerBuilder
          .apply[Task](rts.platform.executor.asEC)
          .bindHttp(8081, "localhost")
          .withHttpApp(Hello1Service.service)
          .serve
          .compile
          .drain
    }

  def run(args: List[String]) =
    server.fold(_ => zio.ExitCode(1), _ => zio.ExitCode(0))
}

object Hello1Service {

  private val dsl = Http4sDsl[Task]
  import dsl._

  val service = HttpRoutes.of[Task] {
    case GET -> Root => Ok("hello!")
  }.orNotFound
}