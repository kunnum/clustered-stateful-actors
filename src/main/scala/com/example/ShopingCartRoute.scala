package com.example

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.cluster.sharding.typed.HashCodeNoEnvelopeMessageExtractor
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}
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

  val messageExtractor =
    new HashCodeNoEnvelopeMessageExtractor[ShoppingCartActor.Command](numberOfShards = 30) {
      override def entityId(message: ShoppingCartActor.Command): String = message.cartId()
    }

  val routes: Route = path("cart"/Segment) { cartId =>

    val shardRegion: ActorRef[ShoppingCartActor.Command] = ClusterSharding(system).init(
      Entity(ShoppingCartActor.TypeKey) { _ => ShoppingCartActor(cartId)}.withMessageExtractor(messageExtractor)
    )

    def add(id: String, quantity: Int): Future[State] = shardRegion ? (ShoppingCartActor.Add(cartId, id, quantity, _))
    def remove(id: String): Future[State] = shardRegion ? (ShoppingCartActor.Remove(cartId, id, _))
    def view(): Future[State] = shardRegion ? (ShoppingCartActor.View(cartId, _))

    system.log.info("HTTP request received")

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
