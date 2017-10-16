package al.challenge.ticket.reservation.system.model.actor

import akka.actor.{ActorRef, ActorRefFactory}
import akka.pattern.{ask, pipe}
import akka.persistence.{PersistentActor, SnapshotOffer}
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success

private[actor] object MovieTicketsBooker {
  trait Event
  case class RegisterMovieEvent(movieInternalId: String, movieTitle: String, availableSeats: Int) extends Event
  case class ReserveSeatEvent(movieInternalId: String) extends Event
  case class MovieTicketsBookerState(movies: Map[String, ActorRef])
}

class MovieTicketsBooker(movieMaker: (ActorRefFactory, String) => ActorRef)
  extends PersistentActor {
  private final implicit val DefaultTimeout: Timeout = 250 millis
  private final val SnapShotInterval = 100


  import MovieTicketsBooker._
  import SupportedOperations._
  import MovieTicketsBookerSupportedOperations.SupportedResponses._
  import SupportedResponses._
  import MovieTicketsBookerSupportedOperations._

  import scala.concurrent.ExecutionContext.Implicits.global

  override def persistenceId = "MovieTicketsBooker"

  private var state = MovieTicketsBookerState(Map())

  override def receiveRecover: Receive = {
    case evt: Event => updateState(evt)
    case SnapshotOffer(_, snapshot: MovieTicketsBookerState) => state = snapshot
  }

  override def receiveCommand: Receive = {
    case RegisterMovie(imdbId, screenId, availableSeats, movieTitle) => registerMovie(imdbId, screenId, availableSeats, movieTitle)
    case ReserveSeat(imdbId, screenId) => reserveSeat(imdbId, screenId)
    case GetMovieInfo(imdbId, screenId) => loadMovieInfo(imdbId, screenId)
    case WarmUp => registerMovie(Integer.MAX_VALUE.toString, Integer.MAX_VALUE.toString, -1, "warmup")
    case _ =>
  }

  private def registerMovie(imdbId: String, screenId: String, availableSeats: Int, movieTitle: String): Unit =
    withMovie(imdbId, screenId)((_, _) => sender ! MovieAlreadyExist) { movieInternalId =>
      val replyTo = sender
      persist(RegisterMovieEvent(movieInternalId, movieTitle, availableSeats)) {
        event => updateState(event) pipeTo replyTo; saveSnapshotIfRequired()
      }
    }

  private def reserveSeat(imdbId: String, screenId: String): Unit =
    withMovie(imdbId, screenId) { (_, movieInternalId) =>
      val replyTo = sender
      persist(ReserveSeatEvent(movieInternalId)) {
        event => updateState(event) pipeTo replyTo; saveSnapshotIfRequired()
      }
    }()


  private def updateState: Event => Future[Any] = {
    case RegisterMovieEvent(movieInternalId, movieTitle, availableSeats) =>
      val newMovie = movieMaker(context, movieInternalId)
      state = MovieTicketsBookerState(state.movies + (movieInternalId -> newMovie))
      newMovie ? MovieSupportedOperations.RegisterMovie(movieTitle, availableSeats)

    case ReserveSeatEvent(movieInternalId) => state.movies(movieInternalId) ? MovieSupportedOperations.ReserveSeat
  }

  private def saveSnapshotIfRequired(): Unit =
    if (lastSequenceNr % SnapShotInterval == 0 && lastSequenceNr != 0) saveSnapshot(state)

  private def loadMovieInfo(imdbId: String, screenId: String): Unit = {
    val replyTo = sender()
    withMovie(imdbId, screenId) { (movie, _) =>
      movie ? MovieSupportedOperations.GetMovieInfo onComplete {
        case Success(MovieSupportedOperations.SupportedResponses.MovieInformation(movieState)) =>
          replyTo ! MovieInformation(imdbId, screenId, movieState.movieTitle, movieState.availableSeats, movieState.reservedSeats)
        case _ => replyTo ! OperationFailed
      }
    }()
  }

  private def withMovie(imdbId: String, screenId: String)
                       (onMovie: (ActorRef, String) => Unit)
                       (onNone: String => Unit = _ => sender ! MovieDoesNotExist): Unit = {
    val movieInternalId = createMovieInternalId(imdbId, screenId)
    state.movies.get(movieInternalId) match {
      case Some(movie) => onMovie(movie, movieInternalId)
      case None => onNone(movieInternalId)
    }
  }

  private def createMovieInternalId(imdbId: String, screenId: String) = s"$imdbId-$screenId"
}
