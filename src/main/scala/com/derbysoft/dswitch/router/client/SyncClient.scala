package com.derbysoft.dswitch.router.client

import com.typesafe.config.ConfigFactory
import java.util.concurrent.TimeUnit._
import com.derbysoft.dswitch.router.core.{Error, ResponseStatus, ResponseMessage, MessageBody}
import java.util.concurrent.ConcurrentHashMap
import concurrent.forkjoin.{LinkedTransferQueue, TransferQueue}
import akka.actor.{ActorSystem, Actor, Props}

class ResponseActor extends Actor {
  def receive = {
    case ResponseMessage(message, status) ⇒ {
      MessageDispatcher.dispatch(message, status)
    }
  }
}

class SyncClient(val host: String, val timeout: Long) {

  val system = ActorSystem("SyncClient", ConfigFactory.load.getConfig("normal"))

  val responseActor = system.actorOf(Props[ResponseActor])

  val client = new Client(host, responseActor)

  def send(message: MessageBody): ResponseMessage = {
    if (MessageDispatcher.size > 10000) {
      return createTimeoutResponse(message)
    }
    client.send(message)
    val response = waitForResponse(message)
    if (response == null) {
      return createTimeoutResponse(message)
    }
    return response
  }

  private def createTimeoutResponse(message: MessageBody): ResponseMessage = {
    ResponseMessage(new MessageBody(message.uri, message.taskId, message.source, message.destination, message.extensions, None), new Error(message.source, "Timeout", "timeout"))
  }

  private def waitForResponse(message: MessageBody): ResponseMessage = {
    val queue = MessageDispatcher.register(message)
    val response: ResponseMessage = queue.poll(timeout, SECONDS)
    return response;
  }

}

private object MessageDispatcher {

  val map = new ConcurrentHashMap[String, TransferQueue[ResponseMessage]]

  def size(): Int = {
    map.size()
  }


  def dispatch(message: MessageBody, status: ResponseStatus) = {
    map.get(message.taskId).add(ResponseMessage(message, status))
    map.remove(message.taskId)
  }

  def register(message: MessageBody): TransferQueue[ResponseMessage] = {
    val queue = new LinkedTransferQueue[ResponseMessage]
    map.put(message.taskId, queue)
    return queue
  }
}
