package app.model

import org.joda.time.{DateTime, Interval, LocalDateTime}
import server.ServiceFactory.systemClock

//TODO: lose the var and lets just make a new Incident
case class Incident(id: Long, probe: Probe, start: DateTime, var finish: Option[DateTime], var failures: List[String]) {
  def isOpen = finish.isEmpty
  def openDuration = new Interval(start, finish.getOrElse(systemClock().dateTime)).toPeriod
}
