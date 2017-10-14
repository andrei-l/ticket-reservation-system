package al.challenge.ticket.reservation.system.model.exchange

case class ReserveSeatRequest(imdbId: String, availableSeats: Int, screenId: String)
