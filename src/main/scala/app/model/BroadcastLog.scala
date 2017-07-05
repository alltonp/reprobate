package app.model

import org.joda.time.{DateTime, Interval, Hours, LocalDateTime}
import server.ServiceFactory.systemClock

case class Broadcast(messages: List[String], env: String, durationSeconds: Int) {
  val start = systemClock().dateTime
  val finish = start.plusSeconds(durationSeconds)

  private val interval = new Interval(start, finish)

  def isWithinWindow(now: DateTime) = interval.contains(now)
}

case class BroadcastLog() {
  private var broadcasts = List[Broadcast]()

  def update(update: Broadcast) {
    broadcasts = update :: broadcasts
  }

  //TODO: ideally we would remove the broadcasts from memory
  def mostRecent = {
    val now = systemClock().dateTime
    broadcasts.filter(b => Hours.hoursBetween(b.start, now).getHours < 24)
  }

  def notInAReleaseWindow(probe: Probe) = {
    val now = systemClock().dateTime
    broadcasts.find(b => b.isWithinWindow(now) && b.env == probe.env).isEmpty
  }
}
