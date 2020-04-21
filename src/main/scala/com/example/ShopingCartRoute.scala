package com.example

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.cluster.sharding.typed.HashCodeNoEnvelopeMessageExtractor
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityRef}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.example.ShoppingCartActor._
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._

class ShopingCartRoute(val system: ActorSystem[_]) {

  implicit val timeout: Timeout = 5.seconds
  implicit val scheduler = system.scheduler
  implicit val cartFormat = jsonFormat1(State)
  implicit val ec = system.executionContext

  val sharding = ClusterSharding(system)
  sharding.init(Entity(TypeKey)(createBehavior = entityContext => ShoppingCartActor(system, entityContext.entityId)))

  val routes: Route = path("cart"/Segment) { cartId =>

    val entityRef: EntityRef[ShoppingCartActor.Command] = sharding.entityRefFor(TypeKey, cartId)

    def add(id: String, quantity: Int): Future[State] = entityRef ? (ShoppingCartActor.Add(cartId, id, quantity, _))
    def remove(id: String): Future[State] = entityRef ? (ShoppingCartActor.Remove(cartId, id, _))
    def view(): Future[State] = entityRef ? (ShoppingCartActor.View(cartId, _))

    system.log.info("***************** HTTP request received " + cartId)

    concat(
      pathEnd {
        concat(
          post {
            parameters(('id, 'quantity.as[Int])) { (id, quantity) =>
              complete {
                add(id, quantity).map(_.toJson.prettyPrint)
              }
            }
          } ~ delete {
            parameters(('id)) { (id) =>
              complete {
                remove(id).map(_.toJson.prettyPrint)
              }
            }
          } ~ get {
            complete {
              view().map(_.toJson.prettyPrint)
            }
          }
        )
      }
    )
  }

}
