package ru.infobis.zio.reports.repository

import java.util.UUID

import cats.implicits._
import com.mongodb.BasicDBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoDB
import com.mongodb.casbah.query.Imports.{BasicDBList, MongoDBObject}
import ru.infobis.zio.reports.Report
import salat.annotations._
import salat.dao._
import salat.global._
import zio.blocking.{Blocking, effectBlockingCancelable}
import zio.{RIO, UIO, ZLayer}


case class Message(@Key("_id") id: Option[Long], sourceId: Long, mTime: Long, mType: Long, filterMessageType: Option[Long] = None)

class MongoReportsRepository() extends {
  private val client: MongoClient = MongoClient(host = "10.0.14.214")
} with SalatDAO[Message, Long](collection = client("kdv")("messages"))
  with ReportsRepository.Service {

  val uuid: UUID = UUID.randomUUID()
  val reportId = s"reportId_$uuid"

  def killOp(dB: MongoDB, op: Int): DBObject = {
    dB.getCollection("$cmd.sys.killop").findOne(MongoDBObject("op" -> op))
  }

  def getList[A](obj: DBObject, name: String): List[A] =
    (List() ++ obj(name).asInstanceOf[BasicDBList]) map {
      _.asInstanceOf[A]
    }

  def findOpids(): Int = {


    val mongoClient: MongoClient = MongoClient(
      host = "10.0.14.214"
    )
    val dB: MongoDB = MongoDB(mongoClient, "admin")


    val ops: DBObject = dB.getCollection("$cmd.sys.inprog").findOne(MongoDBObject("$all" -> true))
    val activeOps: BasicDBObject = getList[BasicDBObject](ops, "inprog").filter(_.getBoolean("active")).maxBy(_.getInt("secs_running"))
    activeOps.getInt("opid")

  }

  def cancelGetById(): UIO[Unit] = {
    UIO {
      val mongoClient: MongoClient = MongoClient(host = "10.0.14.214")
      val dB: MongoDB = MongoDB(mongoClient, "admin")

      val ops: DBObject = dB.getCollection("$cmd.sys.inprog").findOne(MongoDBObject("$all" -> true))
      val activeOps: List[BasicDBObject] = getList[BasicDBObject](ops, "inprog").filter(_.getBoolean("active"))

      val reportOps: List[BasicDBObject] = activeOps.filter(
        _.getAs[BasicDBObject]("query").flatMap {
          _.getAs[BasicDBObject]("query").map {
            _.contains(reportId)
          }
        }.getOrElse(false)
      )

      reportOps.foreach {
        op => killOp(dB, op.getInt("opid"))
      }

    }
  }

  override def getById(id: Long): RIO[Blocking, Option[Report]] = effectBlockingCancelable {
    count(
      MongoDBObject(
        "_id" -> MongoDBObject("$gte" -> id),
        reportId -> MongoDBObject("$exists" -> false)
      )
    ).some.map {
      res => {
        val ops = findOpids()

        new Report(id, res.toString + ops)
      }
    }
  }(cancelGetById())


}

object MongoReportsRepository {
  val layer: ZLayer[Any, Nothing, ReportsRepository] =
    ZLayer.succeed {
      new MongoReportsRepository()
    }
}
