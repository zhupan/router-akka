package com.derbysoft.dswitch.router.core


trait Rule {

  def sourceHost(message: MessageBody): String

  def destinationHost(message: MessageBody): String

}