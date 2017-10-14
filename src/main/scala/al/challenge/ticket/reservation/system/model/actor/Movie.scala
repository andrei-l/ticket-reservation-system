package al.challenge.ticket.reservation.system.model.actor

import akka.actor.Actor

case class MovieState(imdbId: String,
                      screenId: String,
                      movieTitle: String,
                      availableSeats: Int,
                      reservedSeats: Int = 0)

class Movie extends Actor {

  import SupportedOperations.MovieSupportedOperations._
  import SupportedResponses._

  private var state: Option[MovieState] = None

  override def receive: Receive = {
    case RegisterMovie(imdbId, screenId, availableSeats, movieTitle) =>
      state = Some(MovieState(imdbId, screenId, movieTitle, availableSeats))
      context.become(movieRegistered)
      sender ! MovieRegistered
  }

  private def movieRegistered: Receive = {
    case ReserveSeat =>
      state = state.map({
        case _state@MovieState(_, _, _, availableSeats, reservedSeats) if availableSeats > 0 =>
          sender ! SeatReserved
          _state.copy(availableSeats = availableSeats - 1, reservedSeats = reservedSeats + 1)
        case _state =>
          sender ! CannotReserveSeat("All tickets have been already reserved")
          _state
      })
    case GetInfo => state.foreach(sender ! MovieInformation(_))
  }
}
