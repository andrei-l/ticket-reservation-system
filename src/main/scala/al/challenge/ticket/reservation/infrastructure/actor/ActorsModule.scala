package al.challenge.ticket.reservation.infrastructure.actor

import akka.actor.{ActorRef, ActorRefFactory, ActorSystem, Props}
import al.challenge.ticket.reservation.system.model.actor.{Movie, MovieTicketsBooker}

import scala.concurrent.ExecutionContext
import scala.language.postfixOps


trait ActorsModule {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContext

  lazy val movieTicketsBooker: ActorRef = system.actorOf(
    Props(classOf[MovieTicketsBooker], (f: ActorRefFactory, movieInternalId: String) => f.actorOf(Props[Movie], movieInternalId))
  )
}
