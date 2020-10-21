package ru.infobis.zio.reports.repository

import ru.infobis.mock.FatCalculation
import ru.infobis.zio.reports._
import zio._
import zio.blocking._

final private class InMemoryReportRepository() extends ReportsRepository.Service with FatCalculation {

  override def getById(id: Long): RIO[Blocking, Option[Report]] = effectBlockingInterrupt(veryLongAndFatReportById(id, requestDurationInSeconds))

}

object InMemoryReportRepository {

  val layer: ZLayer[Any, Nothing, ReportsRepository] =
    ZLayer.succeed {
      new InMemoryReportRepository()
    }
}
