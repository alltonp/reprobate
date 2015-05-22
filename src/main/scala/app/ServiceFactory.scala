package app

import app.server.ProbeProviderActor
import im.mange.little.clock.{Clock, RealClock}
import im.mange.little.date.DateFormatForHumans
import net.liftweb.http._
import org.joda.time.DateTimeZone

object ServiceFactory extends Factory {
  lazy val probeProviderActorInstance: ProbeProviderActor = new ProbeProviderActor
  lazy val clockInstance: Clock = RealClock
  lazy val dateFormatsInstance: DateFormatForHumans = new DateFormatForHumans(systemClock(), DateTimeZone.forID("Europe/London"))

  implicit object probeProviderActor extends FactoryMaker(() ⇒ probeProviderActorInstance)
  implicit object systemClock extends FactoryMaker(() ⇒ clockInstance)
  implicit object dateFormats extends FactoryMaker(() ⇒ dateFormatsInstance)

  def shutdown() {}
}