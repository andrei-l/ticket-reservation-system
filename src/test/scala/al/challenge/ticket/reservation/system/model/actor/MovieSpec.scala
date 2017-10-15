package al.challenge.ticket.reservation.system.model.actor

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class MovieSpec extends TestKit(ActorSystem("MovieSpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {


  var movieActor: Option[ActorRef] = None

  import SupportedOperations.MovieSupportedOperations._
  import SupportedResponses._
  import SupportedOperations.SupportedResponses._

  "A Movie Actor" should {
    "register movie" in {
      val movieActor = system.actorOf(Props[Movie])
      movieActor ! RegisterMovie("some new movie", 1)
      expectMsg(MovieRegistered)
      this.movieActor = Some(movieActor)
    }


    "get movie info before reserving a ticket" in {
      withRegisteredMovie { movie =>
        movie ! GetMovieInfo
        expectMovieInformation(1, 0)
      }
    }

    "reserve seat" in {
      withRegisteredMovie { movie =>
        movie ! ReserveSeat
        expectMsg(SeatReserved)
      }
    }

    "get movie info after reserving a ticket" in {
      withRegisteredMovie { movie =>
        movie ! GetMovieInfo
        expectMovieInformation(1, 1)
      }
    }

    "fail to reserve more sits than available" in {
      withRegisteredMovie { movie =>
        movie ! ReserveSeat
        assert(expectMsgClass(classOf[CannotReserveSeat]).reason === "All tickets have been already reserved")
      }
    }

    "get movie info after failure to reserve a ticket" in {
      withRegisteredMovie { movie =>
        movie ! GetMovieInfo
        expectMovieInformation(1, 1)
      }
    }
  }

  private def withRegisteredMovie(op: ActorRef => AnyRef): Unit = {
    movieActor.map { movie =>
      op(movie)
    } getOrElse (throw new IllegalArgumentException("Movie must be registered"))
  }

  private def expectMovieInformation(expectedAvailableSeats: Int, expectedReservedSeats: Int) = {
    expectMsg(MovieInformation(
      MovieState("some new movie", expectedAvailableSeats, expectedReservedSeats)
    ))
  }


  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
}
