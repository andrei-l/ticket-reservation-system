package al.challenge.ticket.reservation.infrastructure

import akka.http.scaladsl.model.{HttpEntity, MediaTypes}

package object http {
  final val CreateMovieRequestEntity = HttpEntity(MediaTypes.`application/json`,
    s"""
             {
                 "imdbId": "tt0111161",
                 "screenId": "screen_123456",
                 "movieTitle": "The Shawshank Redemption",
                 "availableSeats": 1
             }
         """
  )

  final val ReserveSeatRequestEntity = HttpEntity(MediaTypes.`application/json`,
    """
           {
               "imdbId": "tt0111161",
               "screenId": "screen_123456"
           }
       """
  )

  final val ReserveSeatForAnotherMovieRequestEntity = HttpEntity(MediaTypes.`application/json`,
    """
           {
               "imdbId": "tt6",
               "screenId": "screen_1"
           }
       """
  )
}
