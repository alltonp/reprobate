package app

import app.server.ProbeProviderActor
import im.mange.little.clock.{Clock, RealClock}
import net.liftweb.http._

object ServiceFactory extends Factory {
  lazy val probeProviderActorInstance: ProbeProviderActor = new ProbeProviderActor
  lazy val clockInstance: Clock = RealClock

  implicit object probeProviderActor extends FactoryMaker(() ⇒ probeProviderActorInstance)
  implicit object systemClock extends FactoryMaker(() ⇒ clockInstance)

  def shutdown() {}
}