package app.model

import server.ServiceFactory._
import app.server.{ProbeFailure, ProbeStatus}
import scala.collection.immutable.HashMap

//TODO: we should proable make this like shoreditch, with an apply() to do the parsing
//TODO: should this be called Check now? or CheckConfig?
case class Probe(id: String, config: String) {
  private val bits = config.split(",")

  if (bits.length < 7) throw new RuntimeException("Invalid check: " + config)

  private val host = bits(0)
  private val resource = bits(1)
  val env = bits(2)
  val description = bits(3)
  private val active = bits(4)
  val remedy = bits(5)
  private val defconLevel = bits(6)

  val name = env + " " + description
  val url = host + resource
  val activePeriod = if (active.trim.size > 0) parseActive(active) else ActivePeriod(0, 24)
  val defcon = Defcons(defconLevel)
  val needsProxy = false

  def isActive = activePeriod.includes(systemClock().dateTime)

  private def parseActive(active: String) = {
    val bits = active.split("-")
    ActivePeriod(parseStartHour(bits), parseFinishHour(bits))
  }

  private def parseStartHour(bits: Array[String]) = Integer.parseInt(bits(0))
  private def parseFinishHour(bits: Array[String]) = if (bits.length > 1) Integer.parseInt(bits(1)) else 24
}

case class FailedProbe(probe: Probe, failures: List[String])

case class CurrentProbeStatuses(probes: List[app.model.Probe]) {
  private var probeToStatus = new HashMap[Probe,ProbeStatus]()

  def update(probe: Probe, status: ProbeStatus) {
    probeToStatus = probeToStatus.updated(probe, status)
  }

  def onConfigChanged() {
    probeToStatus = new HashMap[Probe,ProbeStatus]()
  }

  def failures = probeToStatus.flatMap(pts => {
    pts._2 match {
      case ProbeFailure(f) => Some(FailedProbe(pts._1, f))
      case _ => None
    }
  })

  def statuses = probeToStatus
}
