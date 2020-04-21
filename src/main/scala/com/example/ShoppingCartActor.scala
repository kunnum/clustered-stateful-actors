package com.example

import akka.actor.typed.ActorRef
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

object ShoppingCartActor {

  trait Command extends CborSerializable
  case class Add(id: String, quantity: Int, replyTo: ActorRef[State]) extends Command
  case class Remove(id: String, replyTo: ActorRef[State]) extends Command
  case class View(replyTo: ActorRef[State]) extends Command

  trait Event extends CborSerializable
  case class Added(id: String, quantity: Int) extends Event
  case class Removed(id: String) extends Event

  case class State(items: Map[String, Int] = Map.empty) extends CborSerializable

  def apply(): EventSourcedBehavior[Command, Event, State]  = {
    EventSourcedBehavior[Command, Event, State] (
      PersistenceId("ShoppingCart", "CartId"), State(), commandHandler, eventHandler
    )
  }

  def commandHandler(state: State, command: Command): Effect[Event, State] = command match {
    case Add(id, quantity, replyTo) => Effect.persist(Added(id, quantity)).thenRun(replyTo ! _)
    case Remove(id, replyTo) => Effect.persist(Removed(id)).thenRun(replyTo ! _)
    case View(replyTo) => Effect.none.thenRun(replyTo ! _)
  }

  def eventHandler(state: State, event: Event): State = event match {
    case Added(id, quantity) => State(state.items + (id -> quantity))
    case Removed(id) => State(state.items.removed(id))
  }

  val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("Cart")
}
