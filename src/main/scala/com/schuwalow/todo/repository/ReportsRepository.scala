package com.schuwalow.todo.repository

import com.schuwalow.todo.Report
import zio._

object ReportsRepository extends Serializable {

  trait Service extends Serializable {
    def getById(id: Long): UIO[Option[Report]]
  }

  def getById(id: Long): URIO[ReportsRepository, Option[Report]] = ZIO.accessM(_.get.getById(id))

}
