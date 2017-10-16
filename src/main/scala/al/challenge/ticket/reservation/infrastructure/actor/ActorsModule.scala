package al.challenge.ticket.reservation.infrastructure.actor

import akka.actor.{ActorRef, ActorRefFactory, ActorSystem, Props}
import al.challenge.ticket.reservation.system.model.actor.SupportedOperations.MovieTicketsBookerSupportedOperations.WarmUp
import al.challenge.ticket.reservation.system.model.actor.{Movie, MovieTicketsBooker}
import akka.pattern.ask

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.concurrent.duration._
import scala.language.postfixOps



trait ActorsModule {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContext

  lazy val movieTicketsBooker: ActorRef = system.actorOf(
    Props(classOf[MovieTicketsBooker], (f: ActorRefFactory, movieInternalId: String) => f.actorOf(Props[Movie], movieInternalId))
  )

  def afterWarmUp()(op: => Unit): Unit = (movieTicketsBooker ? WarmUp)(10 seconds).onComplete {
    case Success(_) => op
    case Failure(ex) => system.log.error(s"failed to start actors module: ${ex.getMessage}", ex)
  }

}
