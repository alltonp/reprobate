package app.model

import app.server.{ProbeSuccess, ProbeInactive, ProbeFailure}

case class ProbeHistory(probe: Probe, executedCount: Int, failedCount: Int, inactiveCount: Int, incidentCount: Int)

//TODO: is ProbeCheckHistory a better name for this
case class ProbeRunHistory(allProbes: List[Probe], incidentLog: IncidentLog, historicChecksExecuted: Long) {
//  private var history = List[ProbeRun]()
  private var currentRun: Option[ProbeRun] = None
  private var summarisedHistory = allProbes.map(ProbeHistory(_ , 0, 0 ,0, 0))

  //TODO: we should store the summary and not the full history or we will just run out of memory ...
  //TIP: leaving the println here to remind me to do fix it ...

  //TODO: this is really nasty, there must be a better way/
  def add(probeRun: ProbeRun) {
    currentRun.map(cr => {
      summarisedHistory = summarisedHistory.map(ph => {
        val r = cr.resultFor(ph.probe)

        val newPB = r match {
          case Some(ProbeSuccess) => ph.copy(executedCount = ph.executedCount + 1)
          case Some(ProbeFailure(_)) => ph.copy(executedCount = ph.executedCount + 1, failedCount = ph.failedCount + 1)
          case Some(ProbeInactive) => ph.copy(inactiveCount = ph.inactiveCount + 1)
          case _ => ph
        }

        newPB.copy(incidentCount = incidentLog.incidentCount(newPB.probe))
      })
    })

    currentRun = Some(probeRun)
//    history = probeRun :: history
//    println("### history: " + history.size + ":" + history.map(_.when))
    ProbateRegistry.updateChecksExecuted(totalExecuted)
  }

  def totalExecuted = historicChecksExecuted + summarisedHistory.map(_.executedCount).sum + currentRun.map(_.executedCount).sum
  def probesWithHistory = summarisedHistory
}
