package com.schuwalow.todo.repository

import com.schuwalow.todo._
import zio._

final private class InMemoryTodoRepository(ref: Ref[Map[Long, Report]])
  extends ReportsRepository.Service {

  override def getById(id: Long): UIO[Option[Report]] = ref.get.map(_.get(id))

}

object InMemoryTodoRepository {

  val layer: ZLayer[Any, Nothing, ReportsRepository] =
    ZLayer.fromEffect {
      for {
        ref <- Ref.make(Map(10000L -> Report(10000, "eeee")))
      } yield new InMemoryTodoRepository(ref)
    }
}
