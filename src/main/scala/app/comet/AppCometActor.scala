package app.comet

import app.ServiceFactory.probeProviderActor
import app.agent.RootAgent
import app.model.Broadcast
import app.server._
import im.mange.jetboot.Js._

//TODO: push probeProviderActor into the Agent (and rename the agent)
class AppCometActor extends MessageCapturingCometActor with ExceptionHandlingActor {
  private val root = RootAgent(AppCometActor.this)

  override def render = {
    probeProviderActor() ! Subscribe(this)
    root.render
  }

  def handleLowPriority(any: Any) {
    any match {
      case i:Init ⇒ partialUpdate(root.onInit(i.allProbes))
      case u:CurrentRunStatusUpdate ⇒ partialUpdate(root.onCurrentRunStatusUpdate(u))
      case u:ProbeStatusUpdate ⇒ partialUpdate(root.onProbeStatusUpdate(u))
      case u:AllRunsStatusUpdate ⇒ partialUpdate(root.onAllRunsStatusUpdate(u))
      case m:Message ⇒ partialUpdate(root.onMessage(m))
      case m:Broadcast ⇒ partialUpdate(root.onBroadcast(m))
      case SendProbeConfig ⇒ partialUpdate(onSendProbeConfig)
      case SendBroadcasts ⇒ partialUpdate(onShowBroadcasts)
      case r:ProbeConfigResponse ⇒ partialUpdate(root.onProbeConfigResponse(r))
      case r:BroadcastsResponse ⇒ partialUpdate(root.onBroadcastsResponse(r))
      case e ⇒ handleUnexpectedMessage(this, e)
    }
  }

  override def localShutdown() {
    probeProviderActor() ! Unsubscribe(this)
    root.cleanup()
    super.localShutdown()
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