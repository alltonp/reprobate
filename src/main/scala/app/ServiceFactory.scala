package app

import net.liftweb.http
import http._
import app.server.ProbeProviderActor
import org.joda.time.{DateTime, LocalDateTime, LocalDate}
import org.joda.time.DateTimeZone._

trait Clock {
  //TODO: I *think* the localX are the ones we don't want ....
  def localDate: LocalDate
  def localDateTime: LocalDateTime
  def date: LocalDate
  def dateTime: DateTime
}

object RealClock extends Clock {
  def localDate = new LocalDate()
  def localDateTime: LocalDateTime = new LocalDateTime()
  def date = new LocalDate()
  def dateTime = new DateTime()
}

object ServiceFactory extends Factory {
  lazy val probeProviderActorInstance: ProbeProviderActor = new ProbeProviderActor
  lazy val clockInstance: Clock = RealClock

  implicit object probeProviderActor extends FactoryMaker(() ⇒ probeProviderActorInstance)
  implicit object systemClock extends FactoryMaker(() ⇒ clockInstance)

  def shutdown() {}
}