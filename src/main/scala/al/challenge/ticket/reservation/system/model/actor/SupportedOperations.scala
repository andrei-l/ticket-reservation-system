package al.challenge.ticket.reservation.system.model.actor

object SupportedOperations {

  object SupportedResponses {
    case object MovieRegistered
    case object SeatReserved
    case class CannotReserveSeat(reason: String)
    case class OperationFailed(reason: String)
  }

  private[actor] object MovieSupportedOperations {
    case class RegisterMovie(movieTitle: String, availableSeats: Int)
    case object GetMovieInfo
    case object ReserveSeat

    object SupportedResponses {
      case class MovieInformation(movieState: MovieState)
    }
  }

  object MovieTicketsBookerSupportedOperations {
    case class RegisterMovie(imdbId: String, screenId: String, availableSeats: Int, movieTitle: String)
    case class GetMovieInfo(imdbId: String, screenId: String)
    case class ReserveSeat(imdbId: String, screenId: String)

    object SupportedResponses {
      case object MovieAlreadyExist
      case object MovieDoesNotExist
      case class MovieInformation(imdbId: String,
                                  screenId: String,
                                  movieTitle: String,
                                  availableSeats: Int,
                                  reservedSeats: Int)
    }
  }
}
