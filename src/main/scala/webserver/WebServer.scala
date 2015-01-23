package webserver

//import org.eclipse.jetty.server.Server
//import org.eclipse.jetty.server.nio.SelectChannelConnector
//import org.eclipse.jetty.webapp.WebAppContext
//import java.io.File

object WebServer2 extends App {
  import org.eclipse.jetty.server.Server
  import org.eclipse.jetty.webapp.WebAppContext
  import org.eclipse.jetty.server.handler.AllowSymLinkAliasChecker

  val server = new Server(8080)
  val maxFormSize = 20 * 1000 * 1000
  server.setHandler(new WebAppContext("src/main/webapp", "/") {
    addAliasCheck(new AllowSymLinkAliasChecker)
  })
  try {
    server.start()
//    if (args.contains("interruptible")) {
//      Console.readLine("Press Enter to exit")
//      server.stop()
//    }
    server.join()
  }
  catch { case e: Throwable => e.printStackTrace(); System.exit(1) }
  finally System.exit(0)
}

//object WebServer extends App {
//
//  val serverPort = 8080
//  val productionMode = "production"
//  private val server = createServer
//  private val context = createContext
//  server.setHandler(context)
//
//
//  private def createServer = {
//    val server = new Server
//    val selectChannelConnector = new SelectChannelConnector
//    selectChannelConnector.setPort(serverPort)
//    server.setConnectors(Array(selectChannelConnector))
//    server
//  }
//
//  def startServer = {
//    try {
//      println(">>> STARTING EMBEDDED JETTY SERVER")
//      server.start()
//      println(">>> JETTY SERVER STARTED")
//      while (!server.isRunning) Thread.sleep(100)
//    } catch {
//      case exception: Exception => {
//        println("FAILED TO START JETTY SERVER")
//        exception.printStackTrace()
//        throw exception
//      }
//    }
//  }
//
//
//  private def createContext = {
//    val context = new WebAppContext()
//    context.setServer(server)
//    context.setContextPath("/")
//    if(new File("src/main/webapp").exists())
//      context.setWar("src/main/webapp")
//    else {
//      val loader = context.getClass.getClassLoader
//      val war = loader.getResource("webapp").toExternalForm
//      context.setWar(war)
//    }
//    context
//  }
//
//   startServer
// }