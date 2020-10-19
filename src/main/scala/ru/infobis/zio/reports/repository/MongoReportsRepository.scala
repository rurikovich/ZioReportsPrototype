package ru.infobis.zio.reports.repository

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

object TestCAsbah extends App {

  val mongoClient: MongoClient = MongoClient(host = "10.0.14.214")
  val dB: MongoDB = MongoDB(mongoClient, "admin")


  val ops: DBObject = dB.getCollection("$cmd.sys.inprog").findOne(MongoDBObject("$all" -> true))
  val activeOps: List[BasicDBObject] = getList[BasicDBObject](ops, "inprog").filter(_.getBoolean("active"))

  val maxTimeRunningOp: BasicDBObject = activeOps.maxBy(op => op.getInt("secs_running"))


  private val id: Int = maxTimeRunningOp.getInt("opid")
  println(s"maxTimeRunningOp id = $id")
  println(ops)

  killOp(dB, id)


  def getList[A](obj: DBObject, name: String): List[A] =
    (List() ++ obj(name).asInstanceOf[BasicDBList]) map {
      _.asInstanceOf[A]
    }

  def killOp(dB: MongoDB, op: Int): DBObject = {
    dB.getCollection("$cmd.sys.killop").findOne(MongoDBObject("op" -> op))
  }
}


case class Message(@Key("_id") id: Option[Long], sourceId: Long, mTime: Long, mType: Long, filterMessageType: Option[Long] = None)

class MongoReportsRepository() extends {
  private val client: MongoClient = MongoClient(host = "10.0.14.214")
} with SalatDAO[Message, Long](collection = client("kdv")("messages"))
  with ReportsRepository.Service {

  def killOp(dB: MongoDB, op: Int): DBObject = {
    dB.getCollection("$cmd.sys.killop").findOne(MongoDBObject("op" -> op))
  }

  def getList[A](obj: DBObject, name: String): List[A] =
    (List() ++ obj(name).asInstanceOf[BasicDBList]) map {
      _.asInstanceOf[A]
    }

  def findOpids(): Int = {
    val mongoClient: MongoClient = MongoClient(host = "10.0.14.214")
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

      val maxTimeRunningOp: BasicDBObject = activeOps.maxBy(op => op.getInt("secs_running"))


      val id: Int = maxTimeRunningOp.getInt("opid")
      println(s"maxTimeRunningOp id = $id")
      println(ops)

      killOp(dB, id)
      ()
    }
  }

  override def getById(id: Long): RIO[Blocking, Option[Report]] = effectBlockingCancelable {
    count(MongoDBObject("_id" -> MongoDBObject("$gte" -> id))).some.map {
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
