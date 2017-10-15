package al.challenge.ticket.reservation.system.model.actor

import akka.actor.Actor

case class MovieState(movieTitle: String, availableSeats: Int, reservedSeats: Int = 0)

class Movie extends Actor {

  import SupportedOperations.MovieSupportedOperations._
  import SupportedResponses._
  import SupportedOperations.SupportedResponses._

  private var state: Option[MovieState] = None

  override def receive: Receive = {
    case RegisterMovie(movieTitle, availableSeats) =>
      state = Some(MovieState(movieTitle, availableSeats))
      context.become(movieRegistered)
      sender ! MovieRegistered
  }

  private def movieRegistered: Receive = {
    case ReserveSeat =>
      state = state.map({
        case _state@MovieState(_, availableSeats, reservedSeats) if availableSeats > 0 =>
          sender ! SeatReserved
          _state.copy(availableSeats = availableSeats - 1, reservedSeats = reservedSeats + 1)
        case _state =>
          sender ! CannotReserveSeat("All tickets have been already reserved")
          _state
      })
    case GetMovieInfo => state.foreach(sender ! MovieInformation(_))
  }
}
