package com.schuwalow.todo.repository

import com.schuwalow.todo._
import zio._

final private class InMemoryTodoRepository(
                                            ref: Ref[Map[Long, Report]],
                                            counter: Ref[Long])
    extends ReportsRepository.Service {

  override def getAll: UIO[List[Report]] = ref.get.map(_.values.toList)

  override def getById(id: Long): UIO[Option[Report]] = ref.get.map(_.get(id))

  override def delete(id: Long): UIO[Unit] = ref.update(store => store - id).unit

  override def deleteAll: UIO[Unit] = ref.update(_.empty).unit

  override def create(todoItemForm: TodoItemPostForm): UIO[Report] =
    for {
      newId <- counter.updateAndGet(_ + 1)
      todo   = todoItemForm.asTodoItem(newId)
      _     <- ref.update(store => store + (newId -> todo))
    } yield todo

  override def update(
    id: Long,
    todoItemForm: TodoItemPatchForm
  ): UIO[Option[Report]] =
    for {
      oldValue <- getById(id)
      result   <- oldValue.fold[UIO[Option[Report]]](ZIO.succeed(None)) { x =>
                    val newValue = x.update(todoItemForm)
                    ref.update(store => store + (newValue.id -> newValue)) *> ZIO
                      .succeed(
                        Some(newValue)
                      )
                  }
    } yield result
}

object InMemoryTodoRepository {

  val layer: ZLayer[Any, Nothing, ReportsRepository] =
    ZLayer.fromEffect {
      for {
        ref     <- Ref.make(Map.empty[Long, Report])
        counter <- Ref.make(0L)
      } yield new InMemoryTodoRepository(ref, counter)
    }
}
