package app.comet

import app.ServiceFactory.{systemClock, rimServerActor}
import app.server.ModelChanged
import im.mange.jetboot.comet.{Subscribe, Unsubscribe, RefreshableCometActor}
import im.mange.jetboot.page.CometPage
import im.mange.jetboot.{Js, R, Renderable}
import net.liftweb.actor.LiftActor
import net.liftweb.common.Loggable
import net.liftweb.http.S
import net.liftweb.sitemap.Loc

case class AppPage(override val path: String, override val params: Loc.LocParam[Any]*) extends CometPage[AppCometActor]

case class RimPage(override val path: String, override val params: Loc.LocParam[Any]*) extends CometPage[RimCometActor]

case class RimAgent(subscriber: im.mange.jetboot.comet.Subscriber) extends Renderable {
  println("refresh")
  println(s"params: ${S.request.get.params}")

  def render = R(s"hello ${systemClock().dateTime}").render
}

//TODO: get rid of the namespacing whe we have got rid of the reprobate versions
class RimCometActor extends im.mange.jetboot.comet.RefreshableCometActor with im.mange.jetboot.comet.MessageCapturingCometActor with im.mange.jetboot.comet.Subscriber with Loggable {
  override def onCapturedMessage(message: Any, actor: LiftActor) {}

  private var rootAgent: RimAgent = _

  def beforeRefresh() {
    println("beforeRefresh")
    //root.cleanup()
    rimServerActor() ! Unsubscribe(this)
  }

  def doRefresh() {
    println("doRefresh")
    rootAgent = new RimAgent(this)
  }

  def afterRefresh(): Unit = {
    println("afterRefresh")
    rimServerActor() ! Subscribe(this)//; this ! Init()
  }

  def doRender = {
    println("doRender")
    rootAgent.render
  }

  override def localShutdown() {
    println("localShutdown")
    //root.cleanup()
    rimServerActor() ! Unsubscribe(this)
    super.localShutdown()
  }

  override def onMessage = handleMessage :: super.onMessage

  //TODO: pull out commands for all these
  private def handleMessage: PartialFunction[Any, Unit] = {
    case m:ModelChanged => println(m); partialUpdate(Js.nothing)
    case e => logger.error(s"${getClass.getSimpleName}: unexpected message received: $e")
  }
}