package app.model

import server.ServiceFactory.systemClock
import org.joda.time.{DateTime, LocalDateTime}

case class ActivePeriod(startHour: Int, finishHour: Int) {
  def includes(now: DateTime) = {
    val startOfDay = systemClock().date.toDateTimeAtStartOfDay
    now.isAfter(startOfDay.plusHours(startHour)) && now.isBefore(startOfDay.plusHours(finishHour))
  }
}