package repository

import zio.{UIO, URIO, ZIO}

object ReportRepository {

  trait Service extends Serializable {
    def getById(id: Long): UIO[Option[Report]]
  }

  def getById(id: Long): URIO[ReportRepository, Option[Report]] = ZIO.accessM(_.get.getById(id))

}


case class Report(id: Long, body: String)

