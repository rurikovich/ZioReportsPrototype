package ru.infobis.zio.reports.repository

import ru.infobis.zio.reports.Report
import zio.{RIO, ZLayer}
import zio.blocking.{Blocking, effectBlockingInterrupt}
import salat.global._
import salat.annotations._
import salat.dao._
import com.mongodb.casbah.Imports._
import cats.implicits._

case class Message(@Key("_id") id: Option[Long], sourceId: Long, mTime: Long, mType: Long, filterMessageType: Option[Long] = None)

class MongoReportsRepository extends SalatDAO[Message, Long](collection = MongoClient(host = "10.0.14.211")("kdv")("messages"))
  with ReportsRepository.Service {

  override def getById(id: Long): RIO[Blocking, Option[Report]] = effectBlockingInterrupt {

    count(MongoDBObject("_id" -> MongoDBObject("$gte" -> id))).some.map {
      res => {
        new Report(id, res.toString)
      }
    }
  }


}

object MongoReportsRepository {
  val layer: ZLayer[Any, Nothing, ReportsRepository] =
    ZLayer.succeed {
      new MongoReportsRepository()
    }
}
