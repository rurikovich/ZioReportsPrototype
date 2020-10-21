package ru.infobis

import com.mongodb.casbah.Imports.{MongoClient, MongoClientURI}

import scala.util.Try

object TestCasbah extends App {
  val mongoClient = MongoClient(MongoClientURI("mongodb://10.0.14.214:27017/?maxPoolSize=3"))
  val dB = mongoClient("kdv")
  val collection = dB("messages")


  for (_ <- 0 to 10) {
    Try {
      collection.count()
    }
  }


}
