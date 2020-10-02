package com.schuwalow.todo.repository

import com.schuwalow.todo.Report
import zio._
import zio.logging.{Logging, log}

object ReportsRepository extends Serializable {

  trait Service extends Serializable {
    def getById(id: Long): UIO[Option[Report]]
  }


  def getById(id: Long): URIO[ReportsRepository, Option[Report]] = ZIO.accessM(_.get.getById(id))


  def withTracing[RIn, ROut <: ReportsRepository with Logging, E](
    layer: ZLayer[RIn, E, ROut]
  ): ZLayer[RIn, E, ROut] =
    layer >>> ZLayer.fromFunctionMany[ROut, ROut] { env =>
      def trace(call: => String) = log.trace(s"TodoRepository.$call")

      env.update[ReportsRepository.Service] { service =>
        new Service {

          def getById(id: Long): UIO[Option[Report]] = (trace(s"getById($id)") *> service.getById(id)).provide(env)

        }
      }
    }
}
