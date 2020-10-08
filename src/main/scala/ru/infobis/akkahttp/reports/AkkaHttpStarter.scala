package ru.infobis.akkahttp.reports

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import ru.infobis.mock.FatCalculation

import scala.concurrent.Future
import scala.io.StdIn
import scala.util.{Failure, Success}

import scala.concurrent.ExecutionContext.Implicits.global

object AkkaHttpStarter extends FatCalculation {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem(Behaviors.empty, "my-system")
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.executionContext

    val route =
      path("hello") {
        get {
          onComplete(veryLongAndFatReportByIdFuture()) {
            case Success(value) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, value))
            case Failure(ex) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, ex.getMessage))
          }


        }
      }

    val bindingFuture = Http().newServerAt("localhost", 8081).bind(route)

    //    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  private def veryLongAndFatReportByIdFuture(): Future[String] = {
    Future {
      veryLongAndFatReportById(1, requestDurationInSeconds).map(_.body).getOrElse("")
    }
  }
}