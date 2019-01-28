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
  new LittleServer2(port, webAppPath = "webapp")
  println(s"### Started Reprobate on $port")
}

import java.io.File

import org.eclipse.jetty.server.{Server, ServerConnector}
import org.eclipse.jetty.webapp.WebAppContext

class LittleServer2(serverPort: Int, autoStart: Boolean = true, webAppPath: String = "src/main/webapp") {
  private val server = createServer(serverPort)
  private val context = createContext
  server.setHandler(context)

  private def createServer(port: Int) = {
    val server = new Server
    val httpConnector = new ServerConnector(server)
    httpConnector.setPort(port)
    server.setConnectors(Array(httpConnector))
    server
  }

  def start() = try {
    server.start()
  } catch {
    case e: Throwable => e.printStackTrace(); throw e
  }

  private def createContext = {
    val classLoader = getClass.getClassLoader
//    val x = classLoader.getResource(webAppPath)
//    val y = classLoader.getResource("webapp")

    def packagedPath(root: String) = classLoader.getResource(root).toExternalForm

//    def discover(path: String, packaged: String, context: WebAppContext) =
//      if (new File(path).exists()) path else packagedPath("webapp")

    val context = new WebAppContext()
    context.setServer(server)
    context.setContextPath("/")
    context.setClassLoader(classLoader)
//    val moo = discover(webAppPath, "webapp", context)
//    println(s"$webAppPath -> $moo")
    context.setWar(classLoader.getResource("webapp").toExternalForm)
    context
  }

  if (autoStart) start()
}
