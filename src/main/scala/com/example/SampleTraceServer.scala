package com.example

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.Future
import scala.util.{Success, Failure}

import akka.actor.{ ActorRef, ActorSystem }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path
import HttpMethods._

import com.lightbend.cinnamon.akka.stream.CinnamonAttributes._
import com.lightbend.cinnamon.akka.stream.CinnamonAttributes

import io.opentracing.Span
import io.opentracing.util.GlobalTracer

object SampleTraceServer extends App {
  implicit val system: ActorSystem = ActorSystem("sampleTraceServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
 
  val tracer = GlobalTracer.get

  lazy val routes: Route =
    path("server") { 
      get { ctx => 
        Thread.sleep((Math.random() * 500).toInt)
        // Uncomment to see headers payload
        println(s"<><///*> ${ctx.request.headers}")  
        ctx.complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Server Response at ${System.currentTimeMillis}"))
      }
    } ~ 
    path("client1") {
      get { 
        complete(Http().singleRequest(HttpRequest(uri = "http://localhost:8080/server"))) 
      }
    } ~
    path("client2") {
      get {
        val clientSpan: Span = tracer.buildSpan("clientWork").start()
        tracer.scopeManager().activate(clientSpan, false)
        val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://localhost:8080/server"))
        responseFuture.onComplete {
          case _ => clientSpan.finish()
        }          
        complete(responseFuture)        
      }
    }

  Http().bindAndHandle(routes, "localhost", 8080)
  println(s"Server online at http://localhost:8080/")
  Await.result(system.whenTerminated, Duration.Inf)
}
