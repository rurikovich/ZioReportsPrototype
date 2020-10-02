package ru.infobis.zio.reports.repository

import ru.infobis.zio.reports.Report
import zio._

object ReportsRepository extends Serializable {

  trait Service extends Serializable {
    def getById(id: Long): UIO[Option[Report]]
  }

  def getById(id: Long): URIO[ReportsRepository, Option[Report]] = ZIO.accessM(_.get.getById(id))

}
