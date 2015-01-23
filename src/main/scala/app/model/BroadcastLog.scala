package app.model

import org.joda.time.{Hours, LocalDateTime}
import app.ServiceFactory.systemClock

case class Broadcast(messages: List[String], duration: Long, when: LocalDateTime = systemClock().localDateTime)

case class BroadcastLog() {
  private var broadcasts = List[Broadcast]()

  def update(update: Broadcast) {
    broadcasts = update :: broadcasts
  }

  //TODO: ideally we would remove the broadcasts from memory
  def mostRecent = {
    val now = systemClock().localDateTime
    broadcasts.filter(b => Hours.hoursBetween(b.when, now).getHours < 24)
  }
}
