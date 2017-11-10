package al.challenge.ticket.reservation

import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.persistence.cassandra.testkit.CassandraLauncher
import akka.stream.ActorMaterializer
import al.challenge.ticket.reservation.infrastructure.actor.ActorsModule
import al.challenge.ticket.reservation.infrastructure.config.ConfigModule
import al.challenge.ticket.reservation.infrastructure.http.HttpRoutesModule

import scala.concurrent.ExecutionContextExecutor
import scala.language.postfixOps

object WebServer extends App
  with ConfigModule
  with ActorsModule
  with HttpRoutesModule {

  implicit lazy val system: ActorSystem = ActorSystem()
  override implicit def executor: ExecutionContextExecutor = system.dispatcher
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  CassandraLauncher.start(
    new File("target/.cassandra"),
    CassandraLauncher.DefaultTestConfigResource,
    clean = false,
    port = 9042,
    CassandraLauncher.classpathForResources("logback.xml"),
    host = Some("127.0.0.1")
  )

  Http().bindAndHandle(httpRoute.movieTicketSystemRoute, config.loadString("http.host"), config.loadInt("http.port"))
  system.log.info("Application started")
}
