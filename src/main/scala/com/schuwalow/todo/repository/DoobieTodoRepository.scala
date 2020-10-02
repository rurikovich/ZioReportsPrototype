package com.schuwalow.todo.repository

import cats.effect.Blocker
import cats.implicits._
import doobie._
import doobie.free.connection
import doobie.hikari._
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import zio._
import zio.blocking.Blocking
import zio.interop.catz._

import com.schuwalow.todo._
import com.schuwalow.todo.config._

final private class DoobieTodoRepository(xa: Transactor[Task]) extends ReportsRepository.Service {
  import DoobieTodoRepository.SQL

  override def getAll: UIO[List[Report]] =
    SQL.getAll
      .to[List]
      .transact(xa)
      .orDie

  override def getById(id: Long): UIO[Option[Report]] =
    SQL
      .get(id)
      .option
      .transact(xa)
      .orDie

  override def delete(id: Long): UIO[Unit] =
    SQL
      .delete(id)
      .run
      .transact(xa)
      .unit
      .orDie

  override def deleteAll: UIO[Unit] =
    SQL.deleteAll.run
      .transact(xa)
      .unit
      .orDie

  override def create(todoItemForm: TodoItemPostForm): UIO[Report] =
    SQL
      .create(todoItemForm.asTodoPayload)
      .withUniqueGeneratedKeys[Long]("ID")
      .map(id => todoItemForm.asTodoItem(id))
      .transact(xa)
      .orDie

  override def update(
    id: Long,
    todoItemForm: TodoItemPatchForm
  ): UIO[Option[Report]] =
    (for {
      oldItem <- SQL.get(id).option
      newItem  = oldItem.map(_.update(todoItemForm))
      _       <- newItem.fold(connection.unit)(item => SQL.update(item).run.void)
    } yield newItem)
      .transact(xa)
      .orDie
}

object DoobieTodoRepository {

  def layer: ZLayer[Blocking with DatabaseConfig, Throwable, ReportsRepository] = {
    def initDb(cfg: DatabaseConfig.Config): Task[Unit] =
      Task {
        Flyway
          .configure()
          .dataSource(cfg.url, cfg.user, cfg.password)
          .load()
          .migrate()
      }.unit

    def mkTransactor(
      cfg: DatabaseConfig.Config
    ): ZManaged[Blocking, Throwable, HikariTransactor[Task]] =
      ZIO.runtime[Blocking].toManaged_.flatMap { implicit rt =>
        for {
          transactEC <- Managed.succeed(
                          rt.environment
                            .get[Blocking.Service]
                            .blockingExecutor
                            .asEC
                        )
          connectEC   = rt.platform.executor.asEC
          transactor <- HikariTransactor
                          .newHikariTransactor[Task](
                            cfg.driver,
                            cfg.url,
                            cfg.user,
                            cfg.password,
                            connectEC,
                            Blocker.liftExecutionContext(transactEC)
                          )
                          .toManaged
        } yield transactor
      }

    ZLayer.fromManaged {
      for {
        cfg        <- getDatabaseConfig.toManaged_
        _          <- initDb(cfg).toManaged_
        transactor <- mkTransactor(cfg)
      } yield new DoobieTodoRepository(transactor)
    }
  }

  object SQL {
    def create(todo: TodoPayload): Update0 = sql"""
      INSERT INTO TODOS (TITLE, COMPLETED, ORDERING)
      VALUES (${todo.title}, ${todo.completed}, ${todo.order})
      """.update

    def get(id: Long): Query0[Report] = sql"""
      SELECT * FROM TODOS WHERE ID = ${id}
      """.query[Report]

    val getAll: Query0[Report] = sql"""
      SELECT * FROM TODOS
      """.query[Report]

    def delete(id: Long): Update0 = sql"""
      DELETE from TODOS WHERE ID = ${id}
      """.update

    val deleteAll: Update0 = sql"""
      DELETE from TODOS
      """.update

    def update(todoItem: Report): Update0 = sql"""
      UPDATE TODOS SET
      TITLE = ${todoItem.item.title},
      COMPLETED = ${todoItem.item.completed},
      ORDERING = ${todoItem.item.order}
      WHERE ID = ${todoItem.id}
      """.update
  }
}
