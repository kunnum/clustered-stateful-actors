package com.example

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.typesafe.config.ConfigFactory

import scala.util.{Failure, Success}

object Bootstrap extends App {

  val rootBehavior = Behaviors.setup[Nothing] { context =>

    implicit val system = context.system
    context.spawn(ClusterListener(), "ClusterListener")
    startHttpServer(new ShopingCartRoute(context.system).routes, context.system)

    Behaviors.empty
  }

  private def startHttpServer(routes: Route, system: ActorSystem[_]): Unit = {

    implicit val classicSystem: akka.actor.ActorSystem = system.toClassic
    import system.executionContext

    val futureBinding = Http().bindAndHandle(routes, interface = "localhost", 0)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.error("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  val port = args(0).toInt
  val config = ConfigFactory.parseString(s"akka.remote.artery.canonical.port=$port").withFallback(ConfigFactory.load())
  ActorSystem[Nothing](rootBehavior, "shopping-cart", config)


}
