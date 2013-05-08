package com.derbysoft.dswitch.router.util

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object RemoteActorSystem {

  val remoteServer = "RemoteServer"

  val serviceName = "server"

  def apply(configName: String): ActorSystem = {
    ActorSystem(remoteServer, ConfigFactory.load.getConfig(configName))
  }

}

object Url {

  def apply(host: String): String = {
    "akka://" + RemoteActorSystem.remoteServer + "@" + host + "/user/" + RemoteActorSystem.serviceName
  }
}

