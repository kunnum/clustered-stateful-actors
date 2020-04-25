package com.example

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.example.ShoppingCartActor._
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.duration._

class ShopingCartRoute(psCommandActor: ActorRef[ShardingEnvelope[ShoppingCartActor.Command]], val system: ActorSystem[_]) {

  /*
    01. Split roles to endpoint and shard - Done
    02. Gatlin load test - Done
    03. Docker based deployment
    04. K8 based deployment
    05. NGIX load balancing
    06. Get this on GKE
    07. Write article
    08. Look at additional support
    09. CQRS
    
   */

  implicit val timeout: Timeout = 5.seconds
  implicit val scheduler = system.scheduler
  implicit val cartFormat = jsonFormat1(State)
  implicit val ec = system.executionContext

  val sharding = ClusterSharding(system)
  sharding.init(Entity(TypeKey)(createBehavior = entityContext => ShoppingCartActor(system, entityContext.entityId)))

  val foo: ActorRef[ShardingEnvelope[ShoppingCartActor.Command]] = null

  val routes: Route = path("cart"/Segment) { cartId =>

    system.log.info("***************** HTTP request received " + cartId)

    concat(
      pathEnd {
        concat(
          post {
            parameters(('id, 'quantity.as[Int])) { (id, quantity) =>
              complete {
                psCommandActor.ask { ref : ActorRef[State] =>
                  ShardingEnvelope(cartId, ShoppingCartActor.Add(cartId, id, quantity, ref))
                } map(_.toJson.prettyPrint)
              }
            }
          } ~ delete {
            parameters(('id)) { (id) =>
              complete {
                psCommandActor.ask { ref : ActorRef[State] =>
                  ShardingEnvelope(cartId, ShoppingCartActor.Remove(cartId, id, ref))
                } map(_.toJson.prettyPrint)
              }
            }
          } ~ get {
            complete {
              psCommandActor.ask { ref : ActorRef[State] =>
                ShardingEnvelope(cartId, ShoppingCartActor.View(cartId, ref))
              } map(_.toJson.prettyPrint)
            }
          }
        )
      }
    )
  }

}
