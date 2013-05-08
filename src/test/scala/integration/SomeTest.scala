package integration

import com.derbysoft.dswitch.router.core._
import com.derbysoft.dswitch.router.client.{SyncClient, Client}
import akka.actor.{ActorSystem, ActorRef, Props, Actor}
import com.typesafe.config.ConfigFactory
import java.util.UUID
import com.derbysoft.dswitch.router.server.{ActorCreator, Server}

object TestServer {

  class ProcessActor extends Actor {
    def receive = {
      case (sender: ActorRef, message: MessageBody) => {
        println("get message:" + message.uri + "|" + Thread.currentThread().getId() + "|" + this)

        //        Thread.sleep(1000)
        sender ! ResponseMessage(new MessageBody(message.uri + "/response", message.taskId, "GTA", "TDS", "", ""), Successful());
      }
      case _ => {
        println("abcdef")
      }

    }
  }

  class MyActorCreate extends ActorCreator {
    val system = ActorSystem("AServer", ConfigFactory.load.getConfig("server-normal"))

    override def newActor(): ActorRef = {
      system.actorOf(Props[ProcessActor])
    }
  }

  def main(args: Array[String]) {
    new Server(new MyActorCreate)
    println("Started Server")
  }
}

object TestRouter {

  class SimpleRule extends Rule {
    val TDS = "127.0.0.1:2557"
    val GTA = "127.0.0.1:2556"

    val ipMap = Map("TDS" -> TDS, "GTA" -> GTA)

    override def sourceHost(message: MessageBody): String = {
      ipMap(message.source)
    }

    override def destinationHost(message: MessageBody): String = {
      ipMap(message.destination)
    }

  }

  def main(args: Array[String]) {
    new Router(new SimpleRule)
    println("Started router")
  }

}

object TestClient {

  class ResponseActor extends Actor {
    def receive = {
      case ResponseMessage(message, status) ⇒ println(message.taskId)
    }
  }

  val system = ActorSystem("BServer", ConfigFactory.load.getConfig("normal"))
  val responseActor = system.actorOf(Props[ResponseActor], "response")

  def main(args: Array[String]) {

    //    val client = new Client("127.0.0.1:2555", responseActor)
    val client = new Client("127.0.0.1:2557", responseActor)
    println("Started client")
    var i = 0
    while (i < 200000) {
      client.send(new MessageBody("/hotel/avail", UUID.randomUUID().toString, "GTA", "TDS", "", "dfsd"))

      i += 1
    }
    client.send(new MessageBody("/hotel/avail", "finished", "GTA", "TDS", "", "dfsd"))
  }

}

object TestSyncClient {
  def main(args: Array[String]) {

    val client = new SyncClient("127.0.0.1:2555", 1)
    println("Started async client")
    while (true) {
      println(client.send(new MessageBody("/hotel/avail", UUID.randomUUID().toString, "GTA", "TDS", "", "dfsd")).message.taskId)

      Thread.sleep(1)
    }
  }
}

