package repository

import zio.{Task, URIO, ZIO}

object ReportRepositoryModule {

  trait Service extends Serializable {
    def getById(id: Long): Task[Option[Report]]
  }

  def getById(id: Long): URIO[ReportRepository, Option[Report]] = ZIO.accessM(_.get.getById(id))

}


case class Report(id: Long, body: String)

