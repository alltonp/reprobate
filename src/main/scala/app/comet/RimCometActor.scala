package app.comet

import app.ServiceFactory.{systemClock, rimServerActor}
import app.server.ModelChanged
import im.mange.jetboot.comet.{Subscribe, Unsubscribe, RefreshableCometActor}
import im.mange.jetboot.{Js, R, Renderable}
import net.liftweb.actor.LiftActor
import net.liftweb.common.Loggable
import net.liftweb.http.S

case class RimAgent(subscriber: im.mange.jetboot.comet.Subscriber) extends Renderable {
  println("refresh")
  println(s"params: ${S.request.get.params}")

  def render = R(s"hello ${systemClock().dateTime}").render
}

//TODO: get rid of the namespacing whe we have got rid of the reprobate versions
class RimCometActor extends RefreshableCometActor with im.mange.jetboot.comet.MessageCapturingCometActor with im.mange.jetboot.comet.Subscriber with Loggable {
  override def onCapturedMessage(message: Any, actor: LiftActor) {}

  private var rootAgent = RimAgent(this)

  override def render = rootAgent.render

  def beforeRefresh() {
    //root.cleanup()
    rimServerActor() ! Unsubscribe(this)
  }

  def doRefresh() {
    rootAgent = new RimAgent(this)
  }

  def afterRefresh(): Unit = {
    rimServerActor() ! Subscribe(this)//; this ! Init()
  }

  def doRender = {
    rootAgent.render
  }

  override def localShutdown() {
    //root.cleanup()
    super.localShutdown()
  }

  override def onMessage = handleMessage :: super.onMessage

  //TODO: pull out commands for all these
  private def handleMessage: PartialFunction[Any, Unit] = {
    case m:ModelChanged => println(m); partialUpdate(Js.nothing)
    case e => logger.error(s"${getClass.getSimpleName}: unexpected message received: $e")
  }
}