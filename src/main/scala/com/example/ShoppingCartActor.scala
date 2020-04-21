package com.example

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

object ShoppingCartActor {

  trait Command extends CborSerializable {
    def cartId(): String
  }
  case class Add(cartId:String, id: String, quantity: Int, replyTo: ActorRef[State]) extends Command
  case class Remove(cartId:String, id: String, replyTo: ActorRef[State]) extends Command
  case class View(cartId:String, replyTo: ActorRef[State]) extends Command

  trait Event extends CborSerializable
  case class Added(id: String, quantity: Int) extends Event
  case class Removed(id: String) extends Event

  case class State(items: Map[String, Int] = Map.empty) extends CborSerializable

  def apply(cartId: String)  = Behaviors.setup[Command] { context =>

    def commandHandler(state: State, command: Command): Effect[Event, State] = {
      context.log.info("Command received for cart " + command.cartId)
      command match {
        case Add(_, id, quantity, replyTo) => Effect.persist(Added(id, quantity)).thenRun(replyTo ! _)
        case Remove(_, id, replyTo) => Effect.persist(Removed(id)).thenRun(replyTo ! _)
        case View(_, replyTo) => Effect.none.thenRun(replyTo ! _)
      }
    }

    def eventHandler(state: State, event: Event): State = event match {
      case Added(id, quantity) => State(state.items + (id -> quantity))
      case Removed(id) => State(state.items.removed(id))
    }

    EventSourcedBehavior[Command, Event, State] (
      PersistenceId("ShoppingCart", cartId), State(), commandHandler, eventHandler
    )
  }

  val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("Cart")
}
