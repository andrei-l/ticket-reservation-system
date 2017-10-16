package al.challenge.ticket.reservation.infrastructure.http

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit._
import al.challenge.ticket.reservation.infrastructure.actor.ActorsModule
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}

class MovieTicketReservationSystemHttpRoutesIntegrationSpec extends WordSpec
  with Matchers
  with ScalatestRouteTest
  with HttpRoutesModule
  with ActorsModule
  with BeforeAndAfterAll {


  private implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds dilated)

  override protected def beforeAll(): Unit = afterWarmUp(println("Test Started"))


  private final val MovieRegistrationPath = "/movie"
  private final val ReserveSeatPath = "/movie/reserve-seat"
  private final val GetMoviePathFormat = "/movie?imdbId=%s&screenId=%s"

  "Application" should {
    s"register movie via PUT $MovieRegistrationPath" in {
      Put(MovieRegistrationPath, CreateMovieRequestEntity) ~> httpRoute.movieTicketSystemRoute ~> check {
        status shouldBe Created
      }
    }

    s"fail to register duplicate movie via PUT $MovieRegistrationPath" in {
      Put(MovieRegistrationPath, CreateMovieRequestEntity) ~> httpRoute.movieTicketSystemRoute ~> check {
        responseAs[String] shouldBe """{"msg":"Movie already exist"}"""
        status shouldBe BadRequest
      }
    }

    s"reserve a seat for a movie via POST $ReserveSeatPath" in {
      Post(ReserveSeatPath, ReserveSeatRequestEntity) ~> httpRoute.movieTicketSystemRoute ~> check {
        status shouldBe OK
      }
    }

    s"fail to reserve a seat for a movie as there are no seats left via POST $ReserveSeatPath" in {
      Post(ReserveSeatPath, ReserveSeatRequestEntity) ~> httpRoute.movieTicketSystemRoute ~> check {
        responseAs[String] shouldBe """{"msg":"All tickets have been already reserved"}"""
        status shouldBe BadRequest
      }
    }

    s"fail to reserve a seat for a movie as movie does not exist via POST $ReserveSeatPath" in {
      Post(ReserveSeatPath, ReserveSeatForAnotherMovieRequestEntity) ~> httpRoute.movieTicketSystemRoute ~> check {
        responseAs[String] shouldBe """{"msg":"Movie does not exist"}"""
        status shouldBe BadRequest
      }
    }

    s"get movie info via GET ${GetMoviePathFormat.format("tt0111161", "screen_123456")}" in {
      Get(GetMoviePathFormat.format("tt0111161", "screen_123456")) ~> httpRoute.movieTicketSystemRoute ~> check {
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

    s"fail to get movie info as movie does not exist via GET ${GetMoviePathFormat.format("tt011", "screen_123")}" in {
      Get(GetMoviePathFormat.format("tt011", "screen_123")) ~> httpRoute.movieTicketSystemRoute ~> check {
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
