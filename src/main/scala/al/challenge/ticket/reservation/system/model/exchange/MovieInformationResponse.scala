package al.challenge.ticket.reservation.system.model.exchange

case class MovieInformationResponse(imdbId: String,
                                    screenId: String,
                                    movieTitle: String,
                                    availableSeats: Int,
                                    reservedSeats: Int)
