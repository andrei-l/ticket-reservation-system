package al.challenge.ticket.reservation.system.model.actor

import akka.actor.{ActorLogging, ActorRef, ActorRefFactory}
import akka.pattern.{ask, pipe}
import akka.persistence.{PersistentActor, SnapshotOffer}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Success

private[actor] object MovieTicketsBooker {
  trait Event
  case class RegisterMovieEvent(movieInternalId: String) extends Event
  case class MovieTicketsBookerState(savedMovieIds: Set[String])
}

class MovieTicketsBooker(movieMaker: (ActorRefFactory, String) => ActorRef) extends PersistentActor
  with ActorLogging {
  private final implicit val DefaultTimeout: Timeout = 30 seconds


  import MovieTicketsBooker._
  import SupportedOperations._
  import MovieTicketsBookerSupportedOperations.SupportedResponses._
  import SupportedResponses._
  import MovieTicketsBookerSupportedOperations._

  import scala.concurrent.ExecutionContext.Implicits.global

  override def persistenceId = "MovieTicketsBooker"

  private var state = MovieTicketsBookerState(Set())

  override def receiveRecover: Receive = {
    case evt: Event => createChildMovieActor(evt)
    case SnapshotOffer(_, snapshot: MovieTicketsBookerState) => state = snapshot
    case _ =>
  }

  override def receiveCommand: Receive = {
    case RegisterMovie(imdbId, screenId, availableSeats, movieTitle) => registerMovie(imdbId, screenId, availableSeats, movieTitle)
    case ReserveSeat(imdbId, screenId) => reserveSeat(imdbId, screenId)
    case GetMovieInfo(imdbId, screenId) => loadMovieInfo(imdbId, screenId)
    case _ =>
  }

  private val snapShotInterval = 10

  private def registerMovie(imdbId: String, screenId: String, availableSeats: Int, movieTitle: String): Unit =
    withMovie(imdbId, screenId)((_, _) => sender ! MovieAlreadyExist) { movieInternalId =>
      val replyTo = sender
      persist(RegisterMovieEvent(movieInternalId)) {
        event =>
          createChildMovieActor(event) ? MovieSupportedOperations.RegisterMovie(movieTitle, availableSeats) pipeTo replyTo
          state = MovieTicketsBookerState(state.savedMovieIds + movieInternalId)
          if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0) saveSnapshot(state)
      }
    }

  private def createChildMovieActor: Event => ActorRef = {
    case RegisterMovieEvent(movieInternalId) => movieMaker(context, movieInternalId)
  }

  private def reserveSeat(imdbId: String, screenId: String): Unit =
    withMovie(imdbId, screenId) { (movie, _) =>
      movie ? MovieSupportedOperations.ReserveSeat pipeTo sender
    }()

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
    context.child(movieInternalId) match {
      case Some(movie) => onMovie(movie, movieInternalId)
      case None => onNone(movieInternalId)
    }
  }

  private def createMovieInternalId(imdbId: String, screenId: String) = s"$imdbId-$screenId"
}
