package com.derbysoft.dswitch.router.core

import akka.kernel.Bootable
import akka.actor.{Props, Actor}
import com.derbysoft.dswitch.router.util.{RemoteActorSystem, Url}

class Router(val rule: Rule) extends Bootable {

  val system = RemoteActorSystem("router")

  val actor = system.actorOf(Props(new RouterActor(rule)), RemoteActorSystem.serviceName)

  override def startup() {
  }

  override def shutdown() {
    system.shutdown()
  }

}

class RouterActor(rule: Rule) extends Actor {

  override def receive = {
    case RequestMessage(message) => {
      val destActor = context.actorFor(Url(rule.destinationHost(message)))
      destActor ! RequestMessage(message)
    }
    case ResponseMessage(message, status) ⇒ {
      val sourceActor = context.actorFor(Url(rule.sourceHost(message)))
      sourceActor ! ResponseMessage(message, status)
    }
  }

}