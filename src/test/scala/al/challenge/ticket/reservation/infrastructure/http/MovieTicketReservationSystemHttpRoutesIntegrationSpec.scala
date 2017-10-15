package al.challenge.ticket.reservation.infrastructure.http

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import al.challenge.ticket.reservation.infrastructure.actor.ActorsModule
import org.scalatest.{Matchers, WordSpec}

class MovieTicketReservationSystemHttpRoutesIntegrationSpec extends WordSpec
  with Matchers
  with ScalatestRouteTest
  with HttpRoutesModule
  with ActorsModule {


  "Application" should {
    "register movie via Put /movies/register" in {
      val requestEntity = HttpEntity(MediaTypes.`application/json`,
        s"""
               {
                   "imdbId": "tt0111161",
                   "screenId": "screen_123456",
                   "movieTitle": "The Shawshank Redemption",
                   "availableSeats": 1
               }
           """
      )

      Put("/movies/register", requestEntity) ~> httpRoute.movieTicketSystemRoute ~> check {
        status shouldBe Created
      }
    }

    "reserve a seat for a movie via POST /movies/reserve-seat" in {
      val requestEntity = HttpEntity(MediaTypes.`application/json`,
        """
               {
                   "imdbId": "tt0111161",
                   "screenId": "screen_123456"
               }
           """
      )

      Post("/movies/reserve-seat", requestEntity) ~> httpRoute.movieTicketSystemRoute ~> check {
        status shouldBe OK
      }
    }

    "reserve a seat for a movie via GET /movies/get-movie-info?imdbId=x&screenId=y" in {
      Get(s"/movies/get-movie-info?imdbId=tt0111161&screenId=screen_123456") ~> httpRoute.movieTicketSystemRoute ~> check {
        responseAs[String].replaceAll("\\s+", "") shouldBe
          """
               {
                    "imdbId": "tt0111161",
                    "screenId": "screen_123456",
                    "movieTitle": "The Shawshank Redemption",
                    "availableSeats": 1,
                    "reservedSeats": 1
                }
          """.replaceAll("\\s+", "")
        status shouldBe OK
      }
    }
  }

}
