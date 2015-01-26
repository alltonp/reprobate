package app.agent

import app.comet.Subscriber
import im.mange.jetboot.bootstrap3.{Bootstrap, GridSystem}
import app.server._
import im.mange.jetboot.Css._
import app.server.AllRunsStatusUpdate
import app.server.ProbeFailure
import app.server.CurrentRunStatusUpdate
import app.model.{Broadcast, Probe}
import im.mange.jetboot.{Renderable, Composite}
import app.server.ProbeStatusUpdate

//TODO:
//have the probe list be an editable yaml or json
//consider tags to name things e.g #env=host1234 #app=port
//support env/app groups
//mouse over for detail
//split failures into columns: e.g. prod, uat, etc
//think about dismisable alerts using (x) for a period of time ...
//consider having start/stop (or sleep ) button on the currently running probe (for release type stuff)
//rename to reprobate and brand it
//tidy up css
//get a marquee in there, perhaps for probe counts - e.g. http://jsfiddle.net/jonathansampson/XxUXD/
//Add a heart beat, if not received then go red
//Refresh page every hour using JavaScript
//add uptime or last udpated time?
//show how long each probe takes to run (for tuning)
//only update the incidents log after each run, otherwise a bit heavy weight
//only update the graphs after each run
//make sure requested things not be live updating (or maybe do, but dont force all users to)
//think about: investigated by, happy face when all good, links to json original
//when incident is being investigated, make it go amber .... (maybe by clicking on the incident log)

//#next
//consider updating the checks, even if its just once per run and limit only to interested subscribers
//total runs/inactive/success line/bar chart
//some serious tidy up required
//tests!
//fixed chart and/or summary to the bottom of screen
//using one of:
//http://www.cssreset.com/how-to-keep-footer-at-bottom-of-page-with-css/
//http://www.cssreset.com/demos/layouts/how-to-keep-footer-at-bottom-of-page-with-css/#
//http://matthewjamestaylor.com/blog/keeping-footers-at-the-bottom-of-the-page
//render all the bits only on init()
//have a url that will pause for x minutes ... (for release time)
//how to handle probes that need to go via proxy ... hmmmm

//have a button for closed incident toggling - glyphicon glyphicon-info-sign or glyphicon glyphicon-list-alt
//show bad numbers need to icons (could update those in realtime) - careful with dorin bug though
//show badge counts in table

//#bug
//(1) open incident but not appearing with a red box - possibly because its now inactive .... or a page refresh occured
//(2) page not updating until next run again
//(3) incident duration is wrong when incidents longer than 24(?) hours
//(4) probe does not exist - shows no red box

//features:
//store probe duration ...

case class RootAgent(subscriber: Subscriber) extends Renderable {
  import im.mange.jetboot.Html._
  import im.mange.jetboot.js.JsCmdFactory._
  import GridSystem._
  import Bootstrap._

  //TODO: pull out an agent for this ...
  private val allProbesStatus = div(id = "allProbesStatus")
  private val currentProbeAgent = ChecksProgressAgent()
  private val probeSummaryAgent = ChecksSummaryAgent()
  private val incidentsAgent = IncidentsAgent()
  private val statusMessageAgent = StatusMessageAgent()
  private val broadcastFlashAgent = BroadcastFlashAgent()
  private val probeConfigAgent = ChecksConfigAgent()
  private val broadcastsAgent = BroadcastsHistoryAgent()
  private val toggleConfigButton = ToggleCheckConfigButton(this)
  private val toggleBroadcastsButton = ToggleBroadcastsHistoryButton(this)

  private var checkStatusAgents: List[CheckStatusAgent] = _

  def render = <form class="lift:form.ajax"><br/>{layout.render}</form>

  private[agent] def requestConfig = {
    subscriber ! SendProbeConfig
    probeConfigAgent.requestConfig
  }

  private[agent] def hideConfig = probeConfigAgent.hide

  private[agent] def showBroadcasts = {
    subscriber ! SendBroadcasts
    broadcastsAgent.onShowRequest
  }

  private[agent] def hideBroadcasts = broadcastsAgent.onHide

  private def layout = GridSystem.container(
//    GridSystem.row(col(12, messageAgent)),
    GridSystem.row(col(12, Composite(broadcastButton, configButton, statusMessageAgent))),
//    row(col(12, Composite(probeSummaryAgent, span(toggleConfigButton).styles(paddingTop("15px"))))),
    //    row(col(10, currentProbeAgent), col(2, currentProbeAgent2)),
    //TODO: RunStatusAgent - need some better names
    GridSystem.row(col(12, currentProbeAgent)),
    GridSystem.row(col(12, probeSummaryAgent)),
    GridSystem.row(col(12, broadcastFlashAgent)),
    GridSystem.row(col(12, allProbesStatus)),
    GridSystem.row(col(12, incidentsAgent)),
    GridSystem.row(col(12, probeConfigAgent)),
    GridSystem.row(col(12, broadcastsAgent))
  )

  def onInit(allProbes: List[Probe]) = {
    checkStatusAgents = allProbes.map(CheckStatusAgent(_))
    allProbesStatus.fill(Composite(checkStatusAgents:_*))
  }

  def onProbeStatusUpdate(update: ProbeStatusUpdate) =
    update.status match {
      case ProbeSuccess => checkStatusAgents.find(update.probe.id == _.probe.id).fold(nothing){_.onSuccess(update)}
      case f:ProbeFailure => checkStatusAgents.find(update.probe.id == _.probe.id).fold(nothing){_.onFailure(f)}
      case ProbeInactive => nothing
    }

  def onCurrentRunStatusUpdate(update: CurrentRunStatusUpdate) =
    currentProbeAgent.onCurrentRunStatusUpdate(update)

  def onAllRunsStatusUpdate(update: AllRunsStatusUpdate) = probeSummaryAgent.onAllRunsStatusUpdate(update) &
                                                           incidentsAgent.onAllRunsStatusUpdate(update)

  def onProbeConfigResponse(response: ProbeConfigResponse) = probeConfigAgent.show(response)
  def onBroadcastsResponse(response: BroadcastsResponse) = broadcastsAgent.onShowResponse(response)

  def onMessage(message: Message) = statusMessageAgent.onMessage(message)
  def onBroadcast(message: Broadcast) = broadcastFlashAgent.onBroadcast(message)

  def cleanup() {}

  //TODO: de-dupe
  private def configButton = span(toggleConfigButton).classes(pullLeft).styles(paddingTop("9px"), paddingRight("10px"))
  private def broadcastButton = span(toggleBroadcastsButton).classes(pullLeft).styles(paddingTop("9px"), paddingRight("10px"))
}