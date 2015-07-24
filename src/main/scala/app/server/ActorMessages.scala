package app.server

import app.model._
import im.mange.jetboot.comet.Subscriber

//import app.comet.Subscriber
import org.joda.time.LocalDateTime
import scala._
import app.model.Probe
import app.model.ChecksHistory
import app.model.Incident

//TODO: these need to die soon ...
//case class Subscribe(subscriber: im.mange.jetboot.comet.Subscriber)
//case class Unsubscribe(subscriber: im.mange.jetboot.comet.Subscriber)

//TODO: should this be ExecuteProbeSuite?
case object ExecuteProbeRun
case class PreExecuteProbe(probe: Probe)
case class ReallyExecuteProbe(probe: Probe)
case class PostExecuteProbe(probe: Probe, result: ProbeStatus)
case class ProbeStatusUpdate(probe: Probe, status: ProbeStatus, incident: Option[Incident])

sealed trait ProbeStatus
case object ProbeSuccess extends ProbeStatus

case class ProbeFailure(failures: List[String]) extends ProbeStatus
case object ProbeInactive extends ProbeStatus

case class CurrentRunStatusUpdate(success: Int, failure: Int, inactive: Int, of: Int) {
  def successPercent = percentageOfTotal(success)
  def failurePercent = percentageOfTotal(failure)
  def inactivePercent = percentageOfTotal(inactive)
  def pendingPercent = percentageOfTotal(of - (success + failure + inactive))

  private def percentageOfTotal(amount: Int) = if (of <1 ) 0 else ((BigDecimal(amount) / BigDecimal(of)) * 100).doubleValue()
}

//TODO: should this be MessageUpdate
case class Message(subject: String, detail: String)
case class AllRunsStatusUpdate(totalExecuted: Long, totalIncidents: Long, openIncidents: List[Incident], closedIncidents: List[Incident])

//TODO: this is all a bit manky - needing two messages for the request
case object SendProbeConfig
case class ProbeConfigRequest(subscriber: Subscriber)
case class ProbeConfigResponse(probes: List[ChecksHistory])

//TODO: this is all a bit manky - needing two messages for the request
case object SendBroadcasts
case class BroadcastsRequest(subscriber: Subscriber)
case class BroadcastsResponse(broadcasts: List[Broadcast])