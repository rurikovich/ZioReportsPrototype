package ru.infobis.zio.reports.fibers

import java.util.UUID

import ru.infobis.zio.reports.Report
import zio._

object FiberManager {

  trait Service {
    def addFiber(f: Fiber[Throwable, Option[Report]]): Task[UUID]

    def removeFiber(uuid: UUID): Task[Option[Fiber[Throwable, Option[Report]]]]

    def listFibers(): Task[List[UUID]]

    def interruptFiber(uuid: UUID): Task[Exit[Throwable, Option[Report]]]
  }

  def addFiber(f: Fiber[Throwable, Option[Report]]): RIO[FiberManager, UUID] = ZIO.accessM(_.get.addFiber(f))

  def listFibers(): RIO[FiberManager, List[UUID]] = ZIO.accessM(_.get.listFibers())

  def interruptFiber(uuid: UUID): RIO[FiberManager, Exit[Throwable, Option[Report]]] = ZIO.accessM(_.get.interruptFiber(uuid))

  def removeFiber(uuid: UUID): RIO[FiberManager, Option[Fiber[Throwable, Option[Report]]]] = ZIO.accessM(_.get.removeFiber(uuid))

}
