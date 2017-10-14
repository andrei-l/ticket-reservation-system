package al.challenge.ticket.reservation.system.model.actor

object SupportedOperations {

  object MovieSupportedOperations {
    case class RegisterMovie(imdbId: String, screenId: String, availableSeats: Int, movieTitle: String)
    case object GetInfo
    case object ReserveSeat

    object SupportedResponses {
      case object MovieRegistered
      case object SeatReserved
      case class CannotReserveSeat(reason: String)
      case class MovieInformation(movieState: MovieState)
    }
  }
}
