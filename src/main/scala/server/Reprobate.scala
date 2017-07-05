package server

import im.mange.little.LittleServer

//FEATURES:
//use font awesome spinners instead of gifs
//use font awesome icons instead of broadcast and config
//read config from servers, shoreditch style
//make broadcasts and config be live, and show the active/inactiveness of each check
//show current clock time, somewhere prominent - e.g. top right

object Reprobate extends App {
  private val port = 8473
  new LittleServer(port)
  println(s"### Started Reprobate on $port")
}
