package app

import app.ServiceFactory._
import org.joda.time.DateTimeZone._
import org.joda.time.format.DateTimeFormat._
import org.joda.time.format.PeriodFormatterBuilder
import org.joda.time.{Interval, LocalDateTime, Period}

object DateFormatForHumans {
  private val standardTimeFormat = forPattern("HH:mm:ss").withZone(UTC)
  private val standardDateTimeFormat = forPattern("HH:mm:ss EEE dd MMM yyyy").withZone(UTC)
  private val todayDateTimeFormat = forPattern("HH:mm:ss 'Today'").withZone(UTC)
  private val thisYearDateTimeFormat = forPattern("HH:mm:ss EEE dd MMM").withZone(UTC)

  private val agoFormat = new PeriodFormatterBuilder()
    .appendHours()
    .appendSuffix("h")
    .appendSeparator(", ")
    .printZeroRarelyLast()
    .appendMinutes()
    .appendSuffix("m")
    .appendSeparator(", ")
    .appendSeconds()
    .appendSuffix("s")
    .toFormatter

  def format(when: LocalDateTime) = formatFor(when).print(when)
  def ago(when: LocalDateTime) = agoFormat.print(new Interval(when.toDateTime, today.toDateTime).toPeriod)
  def ago(period: Period) = agoFormat.print(period)
  def timeNow = standardTimeFormat.print(today)

  private def formatFor(when: LocalDateTime) = {
    if (isToday(when)) todayDateTimeFormat
    else if (isThisYear(when)) thisYearDateTimeFormat
    else standardDateTimeFormat
  }

  private def isToday(when: LocalDateTime) = isSameDay(when, today)
  private def isThisYear(when: LocalDateTime) = when.isAfter(today.minusYears(1))

  private def isSameDay(when: LocalDateTime, as: LocalDateTime) =
    when.getYear == as.getYear &&
    when.getMonthOfYear == as.getMonthOfYear &&
    when.getDayOfMonth == as.getDayOfMonth

  private def today = systemClock().localDateTime
}
