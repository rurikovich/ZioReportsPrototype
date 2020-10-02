package com.schuwalow.todo.repository

import zio._
import zio.logging.Logging
import zio.logging.log

import com.schuwalow.todo.{  Report, TodoItemPatchForm, TodoItemPostForm }

object ReportsRepository extends Serializable {

  trait Service extends Serializable {
    def getAll: UIO[List[Report]]

    def getById(id: Long): UIO[Option[Report]]

    def delete(id: Long): UIO[Unit]

    def deleteAll: UIO[Unit]

    def create(todoItemForm: TodoItemPostForm): UIO[Report]

    def update(
      id: Long,
      todoItemForm: TodoItemPatchForm
    ): UIO[Option[Report]]
  }

  def create(todoItemForm: TodoItemPostForm): URIO[ReportsRepository, Report] = ZIO.accessM(_.get.create(todoItemForm))

  def getById(id: Long): URIO[ReportsRepository, Option[Report]] = ZIO.accessM(_.get.getById(id))

  val getAll: URIO[ReportsRepository, List[Report]] =
    ZIO.accessM(_.get.getAll)

  def delete(id: Long): URIO[ReportsRepository, Unit] = ZIO.accessM(_.get.delete(id))

  val deleteAll: URIO[ReportsRepository, Unit] =
    ZIO.accessM(_.get.deleteAll)

  def update(
    id: Long,
    todoItemForm: TodoItemPatchForm
  ): URIO[ReportsRepository, Option[Report]] = ZIO.accessM(_.get.update(id, todoItemForm))

  def withTracing[RIn, ROut <: ReportsRepository with Logging, E](
    layer: ZLayer[RIn, E, ROut]
  ): ZLayer[RIn, E, ROut] =
    layer >>> ZLayer.fromFunctionMany[ROut, ROut] { env =>
      def trace(call: => String) = log.trace(s"TodoRepository.$call")

      env.update[ReportsRepository.Service] { service =>
        new Service {
          val getAll: UIO[List[Report]] =
            (trace("getAll") *> service.getAll).provide(env)

          def getById(id: Long): UIO[Option[Report]] = (trace(s"getById($id)") *> service.getById(id)).provide(env)

          def delete(id: Long): UIO[Unit] = (trace(s"delete($id)") *> service.delete(id)).provide(env)

          val deleteAll: UIO[Unit] =
            (trace("deleteAll") *> service.deleteAll).provide(env)

          def create(todoItemForm: TodoItemPostForm): UIO[Report] =
            (trace(s"create($todoItemForm)") *> service.create(todoItemForm))
              .provide(env)

          def update(
            id: Long,
            todoItemForm: TodoItemPatchForm
          ): UIO[Option[Report]] =
            (trace(s"update($id, $todoItemForm)") *> service.update(
              id,
              todoItemForm
            )).provide(env)
        }
      }
    }
}
