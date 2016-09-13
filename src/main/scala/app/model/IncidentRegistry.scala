package app.model

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths, StandardOpenOption}

import net.liftweb.json._
import org.joda.time.DateTime

import scala.io.Source

case class IncidentState(id: Long, name: String, env: String, start: DateTime, finish: Option[DateTime], failures: List[String])
case class IncidentsState(incidents: List[IncidentState])

object IncidentRegistry {
  private val file = new File("incidents.json")

  def load = {
    if (!file.exists()) save(IncidentsState(Nil))
    Json.deserialiseIncidentsState(Source.fromFile(file).getLines().mkString("\n"))
  }

  def updateIncidents(newValue: List[Incident]) {
    synchronized {
      //TODO: this ultimately weill not scale well, ultimately we should write the incident to it's own file
      //TODO: but then need a way to get them sll easily ...
      //TODO: or we could limit to the last x entries, so they eventually fall off ....
      val current = load
      val newIds = newValue.map(_.id)
      val onlyPreviousIncidents = current.incidents.filterNot(i => newIds.contains(i.id))
      save(current.copy(incidents = (newValue.map(i =>
        IncidentState(i.id, i.probe.description, i.probe.env, i.start.toDateTime, i.finish.map(_.toDateTime), i.failures)
      )) ::: onlyPreviousIncidents ))
    }
  }
  
  private def save(state: IncidentsState) {
    //TODO: drop ...
    val jsonAst = Json.serialise(state)
    Files.write(Paths.get(file.getName), pretty(render(jsonAst)).getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
  }
}


