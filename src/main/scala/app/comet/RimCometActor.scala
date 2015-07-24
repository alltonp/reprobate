package app.comet

import im.mange.jetboot.comet.RefreshableCometActor
import im.mange.jetboot.{R, Renderable}
import net.liftweb.actor.LiftActor
import net.liftweb.common.Loggable

case class RimAgent(subscriber: Subscriber) extends Renderable {
  def render = R("hello").render
}

class RimCometActor extends RefreshableCometActor with im.mange.jetboot.comet.MessageCapturingCometActor with Subscriber with Loggable {
  override def onCapturedMessage(message: Any, actor: LiftActor) {}

  private var rootAgent = RimAgent(this)

  override def render = rootAgent.render

  def beforeRefresh() {
    //root.cleanup()
  }

  def doRefresh() {
    rootAgent = new RimAgent(this)
  }

  def afterRefresh() {
  }

  def doRender = rootAgent.render

  override def localShutdown() {
    //root.cleanup()
    super.localShutdown()
  }

  override def onMessage = handleMessage :: super.onMessage

  //TODO: pull out commands for all these
  private def handleMessage: PartialFunction[Any, Unit] = {
    case e => logger.error(s"${getClass.getSimpleName}: unexpected message received: $e")
  }
}