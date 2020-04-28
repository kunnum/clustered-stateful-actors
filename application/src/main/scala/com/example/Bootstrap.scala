package com.example

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ActorSystem, Behavior}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}
import akka.cluster.typed.Cluster
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.example.ShoppingCartActor.TypeKey
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Bootstrap extends App {

  private object RootBehavior {

    def apply() : Behavior[NotUsed] = Behaviors.setup { context =>

      implicit val system = context.system
      context.spawn(ClusterListener(), "ClusterListener")

      val cluster = Cluster(system)
      val sharding = ClusterSharding(system)
      context.log.info(s"starting node with roles: $cluster.selfMember.roles")

      if (cluster.selfMember.hasRole("endpoint")) {

        implicit val ec: ExecutionContextExecutor = context.system.executionContext
        val psEntities = sharding.init(Entity(TypeKey)(ctx => ShoppingCartActor(system, ctx.entityId)))

        val host = if (cluster.selfMember.hasRole("docker") || cluster.selfMember.hasRole("k8s")) "0.0.0.0"
        else "localhost"
        startHttpServer(new ShopingCartRoute(psEntities, context.system).routes, host, context.system)

      } else if (cluster.selfMember.hasRole("sharded")) {
        sharding.init(Entity(TypeKey)(entityContext => ShoppingCartActor(system, entityContext.entityId)))
      }

      Behaviors.empty
    }
  }

  private def startHttpServer(routes: Route, host: String, system: ActorSystem[_]): Unit = {

    implicit val classicSystem: akka.actor.ActorSystem = system.toClassic
    import system.executionContext

    val futureBinding = Http().bindAndHandle(routes, interface = host)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.error("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }

  }

  def startNode(behavior: Behavior[NotUsed], clusterName: String) = {
    val system = ActorSystem(behavior, clusterName, appConfig)
    system.whenTerminated // remove compiler warnings
  }

  private val appConfig = ConfigFactory.load(sys.props("config.resource"))
  private val clusterName = appConfig.getString ("clustering.cluster.name")

  startNode(RootBehavior(), clusterName)

}
