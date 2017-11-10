package al.challenge.ticket.reservation.system.model.actor

import akka.actor.{ActorRef, ActorRefFactory, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class MovieTicketsBookerSpec extends TestKit(ActorSystem("MovieTicketsBookerSpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  private final val MovieTitle = "title"

  private val movieProb = TestProbe()
  private val movieMaker: (ActorRefFactory, String) => ActorRef =
    (context, movieId) => context.actorOf(Props(classOf[TestProbeWrappingForwarder], movieProb.ref), movieId)
  private val movieTicketsBookerActor = system.actorOf(Props(classOf[MovieTicketsBooker], movieMaker))

  import SupportedOperations._
  import MovieTicketsBookerSupportedOperations.SupportedResponses._
  import SupportedResponses._
  import MovieTicketsBookerSupportedOperations._

  "A Movie Tickets Booker Actor" should {
    "register movie" in registerMovie("1", "1", 1)

    "fail to register movie which has already been registered" in {
      registerMovie("2", "2", 1)
      registerMovieFails("2", "2", 1)
    }

    "reserve seat for a movie" in {
      registerMovie("3", "3", 1)
      reserveSeat("3", "3")
    }

    "fail to reserve a seat for movie which does not exist" in {
      reserveSeatFails("4", "4")
    }

    "load movie info" in {
      registerMovie("5", "5", 1)
      assert(loadMovieInfo("5", "5", MovieState(MovieTitle, 1, 1)) === MovieInformation("5", "5", MovieTitle, 1, 1))
    }

    "fail to load movie info for movie which does not exist" in loadMovieInfoFails("6", "6")
  }

  private def registerMovie(imdbId: String, screenId: String, availableSeats: Int): Unit = {
    movieTicketsBookerActor ! RegisterMovie(imdbId, screenId, availableSeats, MovieTitle)
    movieProb.expectMsg(MovieSupportedOperations.RegisterMovie(MovieTitle, availableSeats))
    movieProb.reply(MovieRegistered)

    expectMsg(MovieRegistered)
  }

  private def registerMovieFails(imdbId: String, screenId: String, availableSeats: Int): Unit = {
    movieTicketsBookerActor ! RegisterMovie(imdbId, screenId, availableSeats, MovieTitle)
    expectMsg(MovieAlreadyExist)
  }

  private def reserveSeat(imdbId: String, screenId: String): Unit = {
    movieTicketsBookerActor ! ReserveSeat(imdbId, screenId)
    movieProb.expectMsg(MovieSupportedOperations.ReserveSeat)
    movieProb.reply(SeatReserved)

    expectMsg(SeatReserved)
  }

  private def reserveSeatFails(imdbId: String, screenId: String): Unit = {
    movieTicketsBookerActor ! ReserveSeat(imdbId, screenId)

    expectMsg(MovieDoesNotExist)
  }

  private def loadMovieInfo(imdbId: String,
                            screenId: String,
                            movieState: MovieState): MovieInformation = {
    movieTicketsBookerActor ! GetMovieInfo(imdbId, screenId)
    movieProb.expectMsg(MovieSupportedOperations.GetMovieInfo)
    movieProb.reply(MovieSupportedOperations.SupportedResponses.MovieInformation(movieState))

    expectMsgClass(classOf[MovieInformation])
  }

  private def loadMovieInfoFails(imdbId: String,
                                 screenId: String): Unit = {
    movieTicketsBookerActor ! GetMovieInfo(imdbId, screenId)
    expectMsg(MovieDoesNotExist)
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
}
