package com.schuwalow.todo.http

import io.circe.Encoder
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import zio._
import zio.interop.catz._


import com.schuwalow.todo.repository._

object TodoService {

  def routes[R <: ReportsRepository](rootUri: String): HttpRoutes[RIO[R, ?]] = {
    type TodoTask[A] = RIO[R, A]

    val dsl: Http4sDsl[TodoTask] = Http4sDsl[TodoTask]
    import dsl._

    implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[TodoTask, A] = jsonEncoderOf[TodoTask, A]

    HttpRoutes.of[TodoTask] {
      case GET -> Root / LongVar(id) =>
        for {
          todo <- ReportsRepository.getById(id)
          response <- todo.fold(NotFound())(
            x =>
              Ok(ReportWithUri(rootUri, x))
          )

        } yield response


    }
  }
}
