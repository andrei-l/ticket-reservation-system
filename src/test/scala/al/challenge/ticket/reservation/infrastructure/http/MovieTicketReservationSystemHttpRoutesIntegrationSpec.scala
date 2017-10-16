package al.challenge.ticket.reservation.infrastructure.http

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, MediaTypes}

import scala.concurrent.duration._
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import al.challenge.ticket.reservation.infrastructure.actor.ActorsModule
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import akka.testkit._

import scala.language.{implicitConversions, postfixOps}

class MovieTicketReservationSystemHttpRoutesIntegrationSpec extends WordSpec
  with Matchers
  with ScalatestRouteTest
  with HttpRoutesModule
  with ActorsModule
  with BeforeAndAfterAll {


  private implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds dilated)

  override protected def beforeAll(): Unit = afterWarmUp(println("Test Started"))


  "Application" should {
    "register movie via PUT /movies/register" in {
      Put("/movies/register", CreateMovieRequestEntity) ~> httpRoute.movieTicketSystemRoute ~> check {
        status shouldBe Created
      }
    }

    "fail to register duplicate movie via PUT /movies/register" in {
      Put("/movies/register", CreateMovieRequestEntity) ~> httpRoute.movieTicketSystemRoute ~> check {
        responseAs[String] shouldBe """{"msg":"Movie already exist"}"""
        status shouldBe BadRequest
      }
    }

    "reserve a seat for a movie via POST /movies/reserve-seat" in {
      Post("/movies/reserve-seat", ReserveSeatRequestEntity) ~> httpRoute.movieTicketSystemRoute ~> check {
        status shouldBe OK
      }
    }

    "fail to reserve a seat for a movie as there are no seats left via POST /movies/reserve-seat" in {
      Post("/movies/reserve-seat", ReserveSeatRequestEntity) ~> httpRoute.movieTicketSystemRoute ~> check {
        responseAs[String] shouldBe """{"msg":"All tickets have been already reserved"}"""
        status shouldBe BadRequest
      }
    }

    "fail to reserve a seat for a movie as movie does not exist via POST /movies/reserve-seat" in {
      Post("/movies/reserve-seat", ReserveSeatForAnotherMovieRequestEntity) ~> httpRoute.movieTicketSystemRoute ~> check {
        responseAs[String] shouldBe """{"msg":"Movie does not exist"}"""
        status shouldBe BadRequest
      }
    }

    "get movie info via GET /movies/get-movie-info?imdbId=x&screenId=y" in {
      Get(s"/movies/get-movie-info?imdbId=tt0111161&screenId=screen_123456") ~> httpRoute.movieTicketSystemRoute ~> check {
        responseAs[String].noSpaces shouldBe
          """
               {
                    "imdbId": "tt0111161",
                    "screenId": "screen_123456",
                    "movieTitle": "The Shawshank Redemption",
                    "availableSeats": 1,
                    "reservedSeats": 1
                }
          """.noSpaces
        status shouldBe OK
      }
    }

    "fail to get movie info as movie does not existvia GET /movies/get-movie-info?imdbId=x&screenId=y" in {
      Get(s"/movies/get-movie-info?imdbId=tt011&screenId=screen_123") ~> httpRoute.movieTicketSystemRoute ~> check {
        responseAs[String] shouldBe """{"msg":"Movie does not exist"}"""
        status shouldBe BadRequest
      }
    }
  }


  private implicit def stringToRichString(s: String): RichString = RichString(s)

  private case class RichString(s: String) {
    def noSpaces: String = s.replaceAll("\\s+", "")
  }

}
