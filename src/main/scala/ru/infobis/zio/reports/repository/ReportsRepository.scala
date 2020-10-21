package ru.infobis.zio.reports.repository

import ru.infobis.zio.reports.Report
import zio._
import zio.blocking.Blocking

object ReportsRepository extends Serializable {

  trait Service extends Serializable {
    def getById(id: Long): RIO[Blocking, Option[Report]]
  }

  def getById(id: Long): RIO[ReportsRepository with Blocking, Option[Report]] = ZIO.accessM(_.get.getById(id))

}
