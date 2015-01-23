package webserver

//object WebServer extends App {
//  val server = new Server(8080);
//
//  val context = new WebAppContext();
//  context.setDescriptor(webapp+"/WEB-INF/web.xml");
//  context.setResourceBase("../test-jetty-webapp/src/main/webapp");
//  context.setContextPath("/");
//  context.setParentLoaderPriority(true);
//
//  server.setHandler(context);
//
//  server.start();
//  server.join();
//
//}

//object WebServer2 extends App {
//  import org.eclipse.jetty.server.Server
//  import org.eclipse.jetty.webapp.WebAppContext
//  import org.eclipse.jetty.server.handler.AllowSymLinkAliasChecker
//
//  val server = new Server(8080)
//  val maxFormSize = 20 * 1000 * 1000
//  server.setHandler(new WebAppContext("src/main/webapp", "/") {
//    addAliasCheck(new AllowSymLinkAliasChecker)
//  })
//  try {
//    server.start()
////    if (args.contains("interruptible")) {
////      Console.readLine("Press Enter to exit")
////      server.stop()
////    }
//    server.join()
//  }
//  catch { case e: Throwable => e.printStackTrace(); System.exit(1) }
//  finally System.exit(0)
//}

object WebServer extends App {
  import org.eclipse.jetty.server.Server
  import org.eclipse.jetty.server.nio.SelectChannelConnector
  import org.eclipse.jetty.webapp.WebAppContext
  import java.io.File

  val serverPort = 8080
  val productionMode = "production"
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

  def startServer = {
    try {
      println(">>> STARTING EMBEDDED JETTY SERVER")
      server.start()
      println(">>> JETTY SERVER STARTED")
      while (!server.isRunning) Thread.sleep(100)
    } catch {
      case exception: Exception => {
        println("FAILED TO START JETTY SERVER")
        exception.printStackTrace()
        throw exception
      }
    }
  }


  private def createContext = {
    val context = new WebAppContext()
    context.setServer(server)
    context.setContextPath("/")
    if(new File("src/main/webapp").exists())
      context.setWar("src/main/webapp")
    else {
      val loader = context.getClass.getClassLoader
      val war = loader.getResource("webapp").toExternalForm
      context.setWar(war)
    }
    context
  }

   startServer
}

//import java.io.File
//import org.eclipse.jetty.server.Server
//import org.eclipse.jetty.server.nio.SelectChannelConnector
//import org.eclipse.jetty.webapp.WebAppContext
//import org.eclipse.jetty.servlet.ServletContextHandler
//
//class WebServer(port: Int, autoStart: Boolean = true, webAppPath: String = "src/main/webapp") {
//  private val server = buildServer
//  server.setHandler(createContext)
//
//  def add(path: String) {
//    val context = new ServletContextHandler
//    context.setServer(server)
//    context.setContextPath(path)
//  }
//
//  def start() {
//    try {
//      server.start()
//      println("### Started web server on port %d...".format(port))
//      while (!server.isStarted) Thread.sleep(100)
//    } catch {
//      case e: Exception => {
//        println("### Failed to start web server on port %d".format(port))
//        e.printStackTrace()
//        throw e
//      }
//    }
//  }
//
//  def stop() {
//    server.stop()
//    val end = System.currentTimeMillis() + 10000
//    while (!server.isStopped && end > System.currentTimeMillis()) Thread.sleep(100)
//    if (!server.isStopped) println("!!!!!!! SERVER FAILED TO STOP !!!!!!!")
//  }
//
//  private def buildServer = {
//    val server = new Server
//    val scc = new SelectChannelConnector
//    scc.setPort(port)
//    scc.setAcceptors(Runtime.getRuntime.availableProcessors() * 2)
//    scc.setResponseBufferSize(1000000)
//    server.setConnectors(Array(scc))
//    server.setStopAtShutdown(true)
//    server
//  }
//
//  private def createContext = {
//    val context = new WebAppContext
//    context.setServer(server)
//    context.setContextPath("/")
//    //TIP: jetty won't start if webapp directory doesnt exist - should probably try to create it
//    if (new File(webAppPath).exists()) context.setWar(webAppPath) else context.setWar(getClass.getClassLoader().getResource("webapp").toExternalForm())
//    context
//  }
//
//  OnShutdown.execute("Stop web server", stop _)
//
//  if (autoStart) start()
//}