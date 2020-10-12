package ru.infobis.zio.reports.fibers

import java.util.UUID

import ru.infobis.zio.reports.Report
import ru.infobis.zio.reports.fibers.FiberManager.Service
import zio.Task._
import zio.{Exit, Fiber, Task, UIO, ZLayer}

import scala.collection.concurrent._

class InMemoryFiberManager extends Service {

  val map: Map[UUID, Fiber[Throwable, Option[Report]]] = TrieMap()

  override def addFiber(f: Fiber[Throwable, Option[Report]]): Task[UUID] = Task {
    val uuid = UUID.randomUUID()
    map.addOne((uuid, f))
    uuid
  }

  override def listFibers(): Task[List[UUID]] = Task(map.keys.toList)

  override def interruptFiber(uuid: UUID): Task[Exit[Throwable, Option[Report]]] =
    map.get(uuid) match {
      case Some(f) =>
        val interruptResult: UIO[Exit[Throwable, Option[Report]]] = f.interrupt
        interruptResult.map {
          exit => {
            if(exit.interrupted){
              map.remove(uuid)
            }
            exit
          }
        }
      case None => fail(new Exception(""))
    }

}

object InMemoryFiberManager {
  val layer: ZLayer[Any, Nothing, FiberManager] = ZLayer.succeed(new InMemoryFiberManager())
}
