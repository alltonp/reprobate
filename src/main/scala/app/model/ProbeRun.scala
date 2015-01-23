package app.model

import app.server.{ProbeInactive, ProbeSuccess, ProbeStatus}
import org.joda.time.LocalDateTime
import app.ServiceFactory._
import scala.Some
import app.server.ProbeFailure

case class ProbeRun(probes: List[Probe], when: LocalDateTime = systemClock().localDateTime) {
  private var results = Map[Probe, ProbeStatus]()

  def failureCount = results.values.collect{case ProbeFailure(_) =>()}.size
  def successCount = results.values.count(ProbeSuccess==)
  def ignoreCount = results.values.count(ProbeInactive==)
  def executedCount = successCount + failureCount

  def totalCount = probes.size
  def nextToRun = notYetRun.head
  def runFinished = notYetRun.isEmpty

  def update(probe: Probe, result: ProbeStatus) { results = results.updated(probe, result) }
  def resultFor(probe: Probe) = executedResults.find(_._1 == probe).map(_._2)

  def wasSuccess(probe: Probe) = results.get(probe) match {
    case Some(ProbeSuccess) => true
    case _ => false
  }

  def executedResults: List[(Probe, ProbeStatus)] = results.toList

  private def notYetRun = probes diff results.keys.toList
}
