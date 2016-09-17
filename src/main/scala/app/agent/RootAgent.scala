package app.agent

//import app.comet.Subscriber

import app.agent.columneditor.{ColumnEditableAgent, Column, ColumnConfig, ColumnEditorAgent}
import app.comet.AppCometActor
import app.server._
import im.mange.jetboot._
import im.mange.jetpac._
import app.server.AllRunsStatusUpdate
import app.server.ProbeFailure
import app.server.CurrentRunStatusUpdate
import app.model.{Broadcast, Probe}
import app.server.ProbeStatusUpdate
import im.mange.jetpac.page.CometPage
import net.liftweb.sitemap.Loc

//TODO:
//have the probe list be an editable yaml or json
//consider tags to name things e.g #env=host1234 #app=port
//support env/app groups
//mouse over for detail
//split failures into columns: e.g. prod, uat, etc
//think about dismisable alerts using (x) for a period of time ...
//consider having start/stop (or sleep ) button on the currently running probe (for release type stuff)
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
//tests!
//render all the bits only on init()
//have a url that will pause for x minutes ... (for release time)
//how to handle probes that need to go via proxy ... hmmmm

//have a button for closed incident toggling - glyphicon glyphicon-info-sign or glyphicon glyphicon-list-alt
//show bad numbers need to icons (could update those in realtime) - careful with dorin bug though
//show badge counts in table

//features:
//store probe duration ...

case class AppPage(override val path: String, override val params: Loc.LocParam[Any]*) extends CometPage[AppCometActor]

case class RootAgent(subscriber: im.mange.jetpac.comet.Subscriber) extends Renderable {
  private val allProbesStatus = div(id = Some("allProbesStatus"))
  private val checksProgressAgent = ChecksProgressAgent()
  private val checksSummaryAgent = ChecksSummaryAgent()
  private val incidentsAgent = IncidentsAgent()
  private val statusMessageAgent = StatusMessageAgent()
  private val broadcastFlashAgent = BroadcastFlashAgent()
  private val checksConfigAgent = ChecksConfigAgent()
  private val broadcastsHistoryAgent = BroadcastsHistoryAgent()
  private val toggleCheckConfigButton = ToggleCheckConfigButton(this)
  private val toggleBroadcastsHistoryButton = ToggleBroadcastsHistoryButton(this)

  private val columnEditorAgent = ColumnEditorAgent(
    ColumnConfig(Seq(Column("one", true, false), Column("two", true, false))),
    subscriber, new ColumnEditableAgent() {
      override def onColumnsChanged: Unit = println("changed")
      override def onColumnsSaved: Unit = println("saved")
    }
  )


  private var checkStatusAgents: List[CheckStatusAgent] = _

  def render = <form class="lift:form.ajax"><br/>{layout.render}</form>

  private[agent] def requestConfig = {
    subscriber ! SendProbeConfig
    checksConfigAgent.requestConfig & columnEditorAgent.onInit
  }

  private[agent] def hideConfig = checksConfigAgent.hide

  private[agent] def showBroadcasts = {
    subscriber ! SendBroadcasts
    broadcastsHistoryAgent.onShowRequest
  }

  private[agent] def hideBroadcasts = broadcastsHistoryAgent.onHide

  private def layout = Bs.container(
    Bs.row(col(12, R(broadcastButton, configButton, statusMessageAgent))),
    Bs.row(col(12, checksProgressAgent)),
    Bs.row(col(12, checksSummaryAgent)),
    Bs.row(col(12, broadcastFlashAgent)),
    Bs.row(col(12, allProbesStatus)),
    Bs.row(col(12, incidentsAgent)),
    Bs.row(col(12, checksConfigAgent)),
    Bs.row(col(12, broadcastsHistoryAgent)),
    Bs.row(col(12, columnEditorAgent))
  )

  def onInit(allProbes: List[Probe]) = {
    checkStatusAgents = allProbes.map(CheckStatusAgent(_))
    allProbesStatus.fill(R(checkStatusAgents))
  }

  def onProbeStatusUpdate(update: ProbeStatusUpdate) =
    update.status match {
      case ProbeSuccess => checkStatusAgents.find(update.probe.id == _.probe.id).fold(nothing){_.onSuccess(update)}
      case f:ProbeFailure => checkStatusAgents.find(update.probe.id == _.probe.id).fold(nothing){_.onFailure(f)}
      case ProbeInactive => nothing
    }

  def onCurrentRunStatusUpdate(update: CurrentRunStatusUpdate) = checksProgressAgent.onCurrentRunStatusUpdate(update)

  def onAllRunsStatusUpdate(update: AllRunsStatusUpdate) = checksSummaryAgent.onAllRunsStatusUpdate(update) &
                                                           incidentsAgent.onAllRunsStatusUpdate(update)

  def onProbeConfigResponse(response: ProbeConfigResponse) = checksConfigAgent.show(response)
  def onBroadcastsResponse(response: BroadcastsResponse) = broadcastsHistoryAgent.onShowResponse(response)

  def onMessage(message: Message) = statusMessageAgent.onMessage(message)
  def onBroadcast(broadcast: Broadcast) = broadcastFlashAgent.onBroadcast(broadcast)

  def cleanup() {}

  //TODO: should probably be a ButtonGroup
  private def configButton = span(toggleCheckConfigButton).classes(pullLeft).styles(paddingTop("9px"), paddingRight("10px"))
  private def broadcastButton = span(toggleBroadcastsHistoryButton).classes(pullLeft).styles(paddingTop("9px"), paddingRight("10px"))
}