package server.tea

import app.model._

case class State(incidentLog: IncidentLog, probeRunHistory: ProbeRunHistory, broadcastLog: BroadcastLog, currentRun: ProbeRun, currentProbeStatuses: CurrentProbeStatuses)

case class Model() {
  //TODO: ultimately can be passed in and forgotten about
  //TODO: there might be more model lurking, e.g. for endpoints like incidents etc
  val historicState = ProbateRegistry.load
  val incidentLog = IncidentLog(historicState.incidentsReported)
  val probeRunHistory = ProbeRunHistory(ProbeRegistry.load.map(_.copy()), incidentLog, historicState.checksExecuted)
  val broadcastLog = BroadcastLog()
  var currentRun: ProbeRun = createProbeRun
  val currentProbeStatuses = CurrentProbeStatuses(currentRun.probes)

  def createProbeRun = ProbeRun(ProbeRegistry.load.map(_.copy()))
  def state = State(incidentLog, probeRunHistory, broadcastLog, currentRun, currentProbeStatuses)
}
