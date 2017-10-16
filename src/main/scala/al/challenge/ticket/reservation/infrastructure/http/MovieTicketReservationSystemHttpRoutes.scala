package al.challenge.ticket.reservation.infrastructure.http

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import al.challenge.ticket.reservation.system.model.exchange.{GeneralErrorResponse, MovieInformationResponse, RegisterMovieRequest}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps


private[http] class MovieTicketReservationSystemHttpRoutes(movieTicketsBooker: ActorRef)
                                                          (implicit executionContext: ExecutionContext)
  extends FailFastCirceSupport {

  private final implicit val DefaultTimeout: Timeout = 750 millis

  import al.challenge.ticket.reservation.system.model.actor.SupportedOperations._
  import MovieTicketsBookerSupportedOperations.SupportedResponses._
  import SupportedResponses._
  import MovieTicketsBookerSupportedOperations._


  lazy val movieTicketSystemRoute: Route =
    pathPrefix("movies") {
      path("register") {
        put {
          entity(as[RegisterMovieRequest]) { req =>
            complete(movieTicketsBooker ? RegisterMovie(req.imdbId, req.screenId, req.availableSeats, req.movieTitle) map {
              case MovieRegistered => Created -> None.asJson
              case MovieAlreadyExist => BadRequest -> GeneralErrorResponse("Movie already exist").asJson
            })
          }
        }
      } ~
        path("reserve-seat") {
          post {
            entity(as[ReserveSeat]) { req =>
              complete(movieTicketsBooker ? ReserveSeat(req.imdbId, req.screenId) map {
                case SeatReserved => OK -> None.asJson
                case CannotReserveSeat(reason) => BadRequest -> GeneralErrorResponse(reason).asJson
              })
            }
          }
        } ~
        path("get-movie-info") {
          get {
            parameters('imdbId.as[String], 'screenId.as[String]) { (imdbId, screenId) =>
              complete(movieTicketsBooker ? GetMovieInfo(imdbId, screenId) map {
                case MovieInformation(_imdbId, _screenId, movieTitle, availableSeats, reservedSeats) =>
                  OK -> MovieInformationResponse(_imdbId, _screenId, movieTitle, availableSeats, reservedSeats).asJson
                case MovieDoesNotExist => BadRequest -> GeneralErrorResponse("Movie does not exist").asJson
              })
            }
          }
        }
    }
}
