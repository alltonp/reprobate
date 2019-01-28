package server

import java.io.File

import app.server.RimServerActor
import im.mange.little.clock.{Clock, RealClock}
import im.mange.little.date.DateFormatForHumans
import net.liftweb.http._
import org.joda.time.DateTimeZone
import server.tea.Update

object ServiceFactory extends Factory {
  val dataDir = new File("data")

  if (!dataDir.exists()) {
    println(s"### Creating $dataDir")
    dataDir.mkdir()
  }

  lazy val updateInstance: Update = new Update
  lazy val rimServerActorInstance: RimServerActor = new RimServerActor
  lazy val clockInstance: Clock = RealClock
  lazy val dateFormatsInstance: DateFormatForHumans = new DateFormatForHumans(systemClock(), DateTimeZone.forID("Europe/London"))

  implicit object update extends FactoryMaker(() ⇒ updateInstance)
  implicit object rimServerActor extends FactoryMaker(() ⇒ rimServerActorInstance)
  implicit object systemClock extends FactoryMaker(() ⇒ clockInstance)
  implicit object dateFormats extends FactoryMaker(() ⇒ dateFormatsInstance)

  def shutdown() {}
}