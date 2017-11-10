package al.challenge.ticket.reservation.system.model.actor

import akka.persistence.{PersistentActor, SnapshotOffer}
import al.challenge.ticket.reservation.system.model.actor.Movie.{MovieEvent, MovieRegisteredEvent, SeatReservedEvent}


case class MovieState(movieTitle: String, availableSeats: Int, reservedSeats: Int = 0)

private[actor] object Movie {
  private trait MovieEvent
  private case class MovieRegisteredEvent(movieTitle: String, availableSeats: Int) extends MovieEvent
  private case object SeatReservedEvent extends MovieEvent
}

class Movie extends PersistentActor {
  import SupportedOperations.MovieSupportedOperations._
  import SupportedOperations.SupportedResponses._
  import SupportedResponses._

  private var state: Option[MovieState] = None

  private def updateState: MovieEvent => Unit = {
    case MovieRegisteredEvent(movieTitle, availableSeats) =>
      state = Some(MovieState(movieTitle, availableSeats))
      context.become(movieRegistered)
    case SeatReservedEvent => state = state.map(_state => _state.copy(reservedSeats = _state.reservedSeats + 1))
  }

  override def persistenceId = self.path.name

  override def receiveCommand: Receive = {
    case RegisterMovie(movieTitle, availableSeats) =>
      val replyTo = sender()
      persist(MovieRegisteredEvent(movieTitle, availableSeats)) { event =>
        updateState(event)
        context.system.eventStream.publish(event)
        replyTo ! MovieRegistered
      }
  }

  private val snapShotInterval = 10

  private def movieRegistered: Receive = {
    case ReserveSeat =>
      state.foreach({
        case MovieState(_, availableSeats, reservedSeats) if availableSeats > reservedSeats =>
          val replyTo = sender()
          persist(SeatReservedEvent) { event =>
            replyTo ! SeatReserved
            updateState(event)
            if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0) saveSnapshot(state)
          }
        case _ =>
          sender ! CannotReserveSeat("All tickets have been already reserved")
      })
    case GetMovieInfo => state.foreach(sender ! MovieInformation(_))
  }

  override def receiveRecover = {
    case evt: MovieEvent => updateState(evt)
    case SnapshotOffer(_, snapshot: Option[MovieState]) => state = snapshot
  }
}
