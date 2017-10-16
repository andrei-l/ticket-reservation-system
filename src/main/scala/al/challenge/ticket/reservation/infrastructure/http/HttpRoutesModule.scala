package al.challenge.ticket.reservation.infrastructure.http

import akka.actor.ActorRef
import com.softwaremill.macwire._

import scala.concurrent.ExecutionContext


trait HttpRoutesModule {
  val movieTicketsBooker: ActorRef
  implicit def executor: ExecutionContext

  lazy val httpRoute: MovieTicketReservationSystemHttpRoutes = wire[MovieTicketReservationSystemHttpRoutes]
}
