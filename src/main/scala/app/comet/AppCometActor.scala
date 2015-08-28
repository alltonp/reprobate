package app.comet

import app.ServiceFactory._
import app.agent.RootAgent
import app.model.Broadcast
import app.server._
import im.mange.jetboot._
import im.mange.jetpac._
import im.mange.jetpac.comet._
import net.liftweb.actor.LiftActor
import net.liftweb.common.Loggable

//TODO: push probeProviderActor into the Agent (and rename the agent)
class AppCometActor extends RefreshableCometActor with MessageCapturingCometActor with Subscriber with Loggable {
  override def onCapturedMessage(message: Any) { /*println(s"$this got a $message")*/ }

  private var rootAgent: RootAgent = _

  def beforeRefresh() {
    //root.cleanup()
    probeProviderActor() ! Unsubscribe(this)
  }

  def doRefresh() {
    rootAgent = new RootAgent(this)
  }

  def afterRefresh() = {
    probeProviderActor() ! Subscribe(this)//; this ! Init()
  }

  def doRender = {
    rootAgent.render
  }

  override def localShutdown() {
    probeProviderActor() ! Unsubscribe(this)
    rootAgent.cleanup()
    super.localShutdown()
  }

  override def onMessage = handleMessage :: super.onMessage

  //TODO: pull out commands for all these
  private def handleMessage: PartialFunction[Any, Unit] = {
    case i:Init ⇒ partialUpdate(rootAgent.onInit(i.allProbes))
    case u:CurrentRunStatusUpdate ⇒ partialUpdate(rootAgent.onCurrentRunStatusUpdate(u))
    case u:ProbeStatusUpdate ⇒ partialUpdate(rootAgent.onProbeStatusUpdate(u))
    case u:AllRunsStatusUpdate ⇒ partialUpdate(rootAgent.onAllRunsStatusUpdate(u))
    case m:Message ⇒ partialUpdate(rootAgent.onMessage(m))
    case m:Broadcast ⇒ partialUpdate(rootAgent.onBroadcast(m))
    case SendProbeConfig ⇒ partialUpdate(onSendProbeConfig)
    case SendBroadcasts ⇒ partialUpdate(onShowBroadcasts)
    case r:ProbeConfigResponse ⇒ partialUpdate(rootAgent.onProbeConfigResponse(r))
    case r:BroadcastsResponse ⇒ partialUpdate(rootAgent.onBroadcastsResponse(r))
    case e => logger.error(s"${getClass.getSimpleName}: unexpected message received: $e")
  }

  //TODO: should be show?
  private def onSendProbeConfig = {
    probeProviderActor() ! ProbeConfigRequest(this)
    nothing
  }

  private def onShowBroadcasts = {
    probeProviderActor() ! BroadcastsRequest(this)
    nothing
  }
}