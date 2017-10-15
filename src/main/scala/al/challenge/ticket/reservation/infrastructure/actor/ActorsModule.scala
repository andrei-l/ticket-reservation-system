package al.challenge.ticket.reservation.infrastructure.actor

import akka.actor.{ActorRef, ActorRefFactory, ActorSystem, Props}
import al.challenge.ticket.reservation.system.model.actor.{Movie, MovieTicketsBooker}

trait ActorsModule {
  implicit val system: ActorSystem

  lazy val movieTicketsBooker: ActorRef = system.actorOf(
    Props(classOf[MovieTicketsBooker], (f: ActorRefFactory, movieInternalId: String) => f.actorOf(Props[Movie], movieInternalId))
  )
}
