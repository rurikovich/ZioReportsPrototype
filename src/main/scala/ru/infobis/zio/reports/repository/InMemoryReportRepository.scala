package ru.infobis.zio.reports.repository

import ru.infobis.zio.reports._
import zio._

final private class InMemoryReportRepository(ref: Ref[Map[Long, Report]])
  extends ReportsRepository.Service {

  override def getById(id: Long): UIO[Option[Report]] = ref.get.map(_.get(id))

}

object InMemoryReportRepository {

  val layer: ZLayer[Any, Nothing, ReportsRepository] =
    ZLayer.fromEffect {
      for {
        ref <- Ref.make(Map(10000L -> Report(10000, "eeee")))
      } yield new InMemoryReportRepository(ref)
    }
}
