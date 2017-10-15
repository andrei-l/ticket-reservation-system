package al.challenge.ticket.reservation.system.model.actor

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorRefFactory}
import akka.pattern.{ask, pipe}
import akka.util.Timeout

import scala.collection.mutable
import scala.util.Success

class MovieTicketsBooker(movieMaker: (ActorRefFactory, String) => ActorRef)
  extends Actor {
  private final implicit val DefaultTimeout = Timeout(250, TimeUnit.MILLISECONDS)


  import SupportedOperations._
  import MovieTicketsBookerSupportedOperations.SupportedResponses._
  import SupportedResponses._
  import MovieTicketsBookerSupportedOperations._

  import scala.concurrent.ExecutionContext.Implicits.global

  private val movies = new mutable.HashMap[String, ActorRef]()

  override def receive: Receive = {
    case RegisterMovie(imdbId, screenId, availableSeats, movieTitle) => registerMovie(imdbId, screenId, availableSeats, movieTitle)
    case ReserveSeat(imdbId, screenId) => reserveSeat(imdbId, screenId)
    case GetMovieInfo(imdbId, screenId) => loadMovieInfo(imdbId, screenId)
  }

  private def registerMovie(imdbId: String, screenId: String, availableSeats: Int, movieTitle: String): Unit = {
    withMovie(imdbId, screenId)(_ => sender ! MovieAlreadyExist) { movieInternalId =>
      val newMovie = movieMaker(context, movieInternalId)
      movies += movieInternalId -> newMovie
      (newMovie ? MovieSupportedOperations.RegisterMovie(movieTitle, availableSeats)) pipeTo sender
    }
  }

  private def reserveSeat(imdbId: String, screenId: String): Unit = {
    withMovie(imdbId, screenId) { movie => movie ? MovieSupportedOperations.ReserveSeat pipeTo sender }()
  }

  private def loadMovieInfo(imdbId: String, screenId: String): Unit = {
    val replyTo = sender()
    withMovie(imdbId, screenId) { movie =>
      movie ? MovieSupportedOperations.GetMovieInfo onComplete {
        case Success(MovieSupportedOperations.SupportedResponses.MovieInformation(movieState)) =>
          replyTo ! MovieInformation(imdbId, screenId, movieState.movieTitle, movieState.availableSeats, movieState.reservedSeats)
        case _ => replyTo ! OperationFailed
      }
    }()
  }

  private def withMovie(imdbId: String, screenId: String)
                       (onMovie: ActorRef => Unit)
                       (onNone: String => Unit = _ => sender ! MovieDoesNotExist): Unit = {
    val movieInternalId = createMovieInternalId(imdbId, screenId)
    movies.get(movieInternalId) match {
      case Some(movie) => onMovie(movie)
      case None => onNone(movieInternalId)
    }
  }


  private def createMovieInternalId(imdbId: String, screenId: String) = s"$imdbId-$screenId"
}
