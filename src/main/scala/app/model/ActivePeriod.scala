package app.model

import org.joda.time.{DateTime, LocalDateTime}

case class ActivePeriod(startHour: Int, finishHour: Int) {
  def includes(now: DateTime) = {
    //TODO: this should systemClock()
    val startOfDay = new DateTime(now.getYear, now.getMonthOfYear, now.getDayOfMonth, 0, 0, 0, 0)
    now.isAfter(startOfDay.plusHours(startHour)) && now.isBefore(startOfDay.plusHours(finishHour))
  }
}