package al.challenge.ticket.reservation.system.model.exchange

case class RegisterMovieRequest(imdbId: String, screenId: String, availableSeats: Int, movieTitle: String)
