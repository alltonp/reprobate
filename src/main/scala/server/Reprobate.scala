package server

import im.mange.little.LittleServer

object Reprobate extends App {
  private val port = 8473
  new LittleServer(port)
  println(s"### Started Reprobate on $port")
}
