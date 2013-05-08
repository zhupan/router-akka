package com.derbysoft.dswitch.router.client

import akka.kernel.Bootable
import com.derbysoft.dswitch.router.core.{Message, MessageBody, RequestMessage, ResponseMessage}
import akka.actor._
import com.derbysoft.dswitch.router.util.{RemoteActorSystem, Url}

class Client(val host: String, responseActor: ActorRef) extends Bootable {

  val system = RemoteActorSystem("client")

  val remoteActor = system.actorFor(Url(host))

  val actor = system.actorOf(Props(new ClientActor(responseActor)), RemoteActorSystem.serviceName)

  def send(message: MessageBody) = {
    actor !(remoteActor, RequestMessage(message))
  }

  override def startup() {
  }

  override def shutdown() {
    system.shutdown()
  }

}

class ClientActor(responseActor: ActorRef) extends Actor {

  override def receive = {
    case (actor: ActorRef, message: Message) ⇒ {
      actor ! message
    }
    case ResponseMessage(message, status) ⇒ {
      responseActor ! ResponseMessage(message, status)
    }
  }
}