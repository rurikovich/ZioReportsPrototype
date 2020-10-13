package ru.infobis.zio.reports.repository

import ru.infobis.zio.reports.Report
import zio.{RIO, ZLayer}
import zio.blocking.{Blocking, effectBlockingInterrupt}
import salat.global._
import salat.annotations._
import salat.dao._
import com.mongodb.casbah.Imports._
import cats.implicits._

case class Message(@Key("_id") id: Long, text: String)

class MongoReportsRepository extends SalatDAO[Message, Long](collection = MongoClient()("local")("messages"))
  with ReportsRepository.Service {

  override def getById(id: Long): RIO[Blocking, Option[Report]] = effectBlockingInterrupt {
    find(ref = MongoDBObject("_id" -> MongoDBObject("$gte" -> id))).toList.some.map {
      mList: List[Message] =>{
        new Report(id, mList.toString)
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
