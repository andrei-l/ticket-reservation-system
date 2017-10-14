package al.challenge.ticket.reservation.system.model.exchange

case class RegisterMovieRequest(imdbId: String, availableSeats: Int, screenId: String)
