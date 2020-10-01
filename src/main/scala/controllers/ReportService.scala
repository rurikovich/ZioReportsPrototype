package controllers

import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import repository.{ReportRepository, ReportRepositoryModule}
import zio._

object ReportService {

  def routes[R <: ReportRepository](rootUri: String): HttpRoutes[Task[*]] = {

    type ReportTask[A] = RIO[R, A]



    val dsl: Http4sDsl[ReportTask] = Http4sDsl[ReportTask]
    import dsl._

    implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[ReportTask, A] = jsonOf[ReportTask, A]

    implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[ReportTask, A] = jsonEncoderOf[ReportTask, A]

    HttpRoutes.of[ReportTask] {
      case GET -> Root / LongVar(id) =>
        for {
          todo     <- ReportRepositoryModule.getById(id)
          response <- todo.fold(NotFound())(x => Ok( x))
        } yield response

    }


  }


}
