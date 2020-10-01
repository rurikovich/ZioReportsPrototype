package repository

import zio.{Has, Task, ULayer, ZLayer}

class InMemoryReportRepository extends ReportRepositoryModule.Service {
  override def getById(id: Long): Task[Option[Report]] = Task(Some(Report(id, "body")))
}

object InMemoryReportRepository {
  val inMemoryLayer: ULayer[Has[InMemoryReportRepository]] = ZLayer.succeed {
    new InMemoryReportRepository()
  }
}
