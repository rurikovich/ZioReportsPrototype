package ru.infobis.zio.reports.fibers

import java.util.UUID

import ru.infobis.zio.reports.Report
import zio._

object FiberManager {

  trait Service {
    def addFiber(f: Fiber[Throwable, Report]): Task[UUID]

    def listFibers(): Task[List[UUID]]

    def interruptFiber(uuid: UUID): Task[Exit[Throwable, Report]]
  }


  def addFiber(f: Fiber[Throwable, Report]): RIO[FiberManager, UUID] = ZIO.accessM(_.get.addFiber(f))

  def listFibers(): RIO[FiberManager, List[UUID]] = ZIO.accessM(_.get.listFibers())

  def interruptFiber(uuid: UUID): RIO[FiberManager, Exit[Throwable, Report]] = ZIO.accessM(_.get.interruptFiber(uuid))

}
