package com.derbysoft.dswitch.router.core

trait ResponseStatus

case class Successful() extends ResponseStatus

case class Error(val source: String, val code: String, val msg: String) extends ResponseStatus

trait Message

class MessageBody(val uri: String, val taskId: String, val source: String, val destination: String, val extensions: String, val body: Any) extends Serializable

case class RequestMessage(message: MessageBody) extends Message

case class ResponseMessage(val message: MessageBody, val status: ResponseStatus) extends Message

