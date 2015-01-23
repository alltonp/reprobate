package app.model

import org.joda.time.{Interval, LocalDateTime}
import app.ServiceFactory.systemClock

//TODO: lose the var and lets just make a new Incident
case class Incident(id: Long, probe: Probe, start: LocalDateTime, var finish: Option[LocalDateTime], var failures: List[String]) {
  def isOpen = finish.isEmpty
  def openDuration = new Interval(start.toDateTime, finish.getOrElse(systemClock().localDateTime).toDateTime).toPeriod
}
