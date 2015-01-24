package webserver

//TODO: rename and tidy
object WebServer extends App {
  import org.eclipse.jetty.server.Server
  import org.eclipse.jetty.server.nio.SelectChannelConnector
  import org.eclipse.jetty.webapp.WebAppContext
  import java.io.File

  private val serverPort = 8473
  private val server = createServer
  private val context = createContext
  server.setHandler(context)

  private def createServer = {
    val server = new Server
    val selectChannelConnector = new SelectChannelConnector
    selectChannelConnector.setPort(serverPort)
    server.setConnectors(Array(selectChannelConnector))
    server
  }

  private def startServer() = {
    try {
      println(s"### Starting jetty on $serverPort")
      server.start()
      while (!server.isRunning) Thread.sleep(100)
    } catch {
      case exception: Exception => {
        println("### Failed to start jetty")
        exception.printStackTrace()
        throw exception
      }
    }
  }

  private def createContext = {
    val context = new WebAppContext()
    context.setServer(server)
    context.setContextPath("/")
    if (new File("src/main/webapp").exists())
      context.setWar("src/main/webapp")
    else {
      val loader = context.getClass.getClassLoader
      val war = loader.getResource("webapp").toExternalForm
      context.setWar(war)
    }
    context
  }

   startServer()
}