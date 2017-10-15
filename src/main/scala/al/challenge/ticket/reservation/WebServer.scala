package al.challenge.ticket.reservation

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import al.challenge.ticket.reservation.infrastructure.actor.ActorsModule
import al.challenge.ticket.reservation.infrastructure.config.ConfigModule
import al.challenge.ticket.reservation.infrastructure.http.HttpRoutesModule

import scala.concurrent.ExecutionContextExecutor

object WebServer extends App
  with ConfigModule
  with ActorsModule
  with HttpRoutesModule {

  override implicit val system: ActorSystem = ActorSystem()
  override implicit def executor: ExecutionContextExecutor = system.dispatcher
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()


  Http().bindAndHandle(httpRoute.movieTicketSystemRoute, config.loadString("http.host"), config.loadInt("http.port"))

}
