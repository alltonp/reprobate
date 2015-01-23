package app.model

import org.joda.time.LocalDateTime

case class ActivePeriod(startHour: Int, finishHour: Int) {
  def includes(now: LocalDateTime) = {
    val startOfDay = new LocalDateTime(now.getYear, now.getMonthOfYear, now.getDayOfMonth, 0, 0, 0, 0)
    now.isAfter(startOfDay.plusHours(startHour)) && now.isBefore(startOfDay.plusHours(finishHour))
  }
}