package app.model

import app.ServiceFactory._
import app.server.{ProbeFailure, ProbeStatus, ProbeSuccess}
import org.joda.time.Hours

case class IncidentProbe(probe: Probe, incidentCount: Int)

case class IncidentLog(historicIncidentsReported: Long) {
  private var incidents = List[Incident]()

  def update(probe: Probe, status: ProbeStatus) {
    status match {
      case ProbeSuccess => closeIncidentIfThereIsOne(probe)
      case f:ProbeFailure => onFailure(probe, f)
      case _ => //noop
    }
  }

  def open = incidents.filter(_.isOpen)

  //TODO: ideally we would remove the incidents from memory
  def closed = {
    val now = systemClock().localDateTime
    incidents.filterNot(_.isOpen).filter(i => Hours.hoursBetween(i.finish.get, now).getHours < 24)
  }

  def totalIncidents = historicIncidentsReported + incidents.size
  def incidentCount(probe: Probe) = incidents.filter(_.probe == probe).size
  def currentOpenIncident(probe: Probe) = incidents.find(i => i.probe == probe && i.isOpen)

  //TODO: we should probably have a property for auto-close at midnight
  private def onFailure(probe: Probe, failure: ProbeFailure) {
    if (firstTimeFailed(probe)) openAnIncident(probe, failure)
    else if (incidentOpenTooLong(probe)) closeIncidentIfThereIsOne(probe)
    else updateFailuresForIncident(probe, failure)
  }

  private def closeIncidentIfThereIsOne(probe: Probe) {
    currentOpenIncident(probe).map(i => { i.finish = Some(systemClock().localDateTime) })
    IncidentRegistry.updateIncidents(incidents)
  }

  private def updateFailuresForIncident(probe: Probe, failure: ProbeFailure) {
    currentOpenIncident(probe).map(i => { i.failures = failure.failures })
  }

  private def openAnIncident(probe: Probe, failure: ProbeFailure) {
    incidents = newIncident(totalIncidents + 1, probe, failure) :: incidents
    ProbateRegistry.updateIncidentsReported(totalIncidents)
    IncidentRegistry.updateIncidents(incidents)
  }

  private def newIncident(id: Long, probe: Probe, failure: ProbeFailure) = Incident(id, probe, systemClock().localDateTime, None, failure.failures)
  private def firstTimeFailed(probe: Probe) = currentOpenIncident(probe).isEmpty

  private def incidentOpenTooLong(probe: Probe) = {
    val startOfToday = systemClock().localDate.toDateTimeAtStartOfDay
    currentOpenIncident(probe).fold(false)(i => { startOfToday.isAfter(i.start.toDateTime) })
  }
}