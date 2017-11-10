package al.challenge.ticket.reservation.system.model.actor

import akka.actor.{Actor, ActorRef}

class TestProbeWrappingForwarder(target: ActorRef) extends Actor {
  def receive: Receive = {
    case x => target forward x
  }
}
