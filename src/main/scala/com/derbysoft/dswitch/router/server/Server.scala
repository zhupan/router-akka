package com.derbysoft.dswitch.router.server

import akka.kernel.Bootable
import com.derbysoft.dswitch.router.core.RequestMessage
import com.derbysoft.dswitch.router.util.RemoteActorSystem
import akka.actor.{ActorRef, Props, Actor}

trait ActorCreator {
  def newActor():ActorRef
}

class Server(creator: ActorCreator) extends Bootable {

  val system = RemoteActorSystem("server")

  val actor = system.actorOf(Props(new ServerActor(creator)), RemoteActorSystem.serviceName)

  override def startup() {
  }

  override def shutdown() {
    system.shutdown()
  }
}

class ServerActor(creator: ActorCreator) extends Actor {
  override def receive = {
    case RequestMessage(message) ⇒ {
      creator.newActor() !(sender, message)
    }
  }
}
