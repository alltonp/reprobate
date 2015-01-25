package app.model

import java.io.File
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

import scala.io.Source
import java.nio.file.{StandardOpenOption, Paths, Files}
import net.liftweb.json._
import java.nio.charset.StandardCharsets

case class ProbateState(checksExecuted: Long, incidentsReported: Long)

object ProbateRegistry {
  private val file = new File("state.json")

  def load = {
//    val counter = ProbeIdCounter()
    if (!file.exists()) save(ProbateState(0, 0))
    Json.deserialiseProbateState(Source.fromFile(file).getLines().mkString("\n"))
  }

  def updateIncidentsReported(newValue: Long) {
    synchronized {
      val current = load
      save(current.copy(incidentsReported = newValue))
    }
  }

  def updateChecksExecuted(newValue: Long) {
    synchronized {
      val current = load
      save(current.copy(checksExecuted = newValue))
    }
  }

  private def save(state: ProbateState) {
    val jsonAst = Json.serialise(state)
    Files.write(Paths.get(file.getName), pretty(render(jsonAst)).getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
  }
}

class DateTimeSerializer extends Serializer[DateTime] {
  private val pattern = ISODateTimeFormat.dateTime()
  private val TheClass = classOf[DateTime]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), DateTime] = {
    case (TypeInfo(TheClass, _), json) ⇒ json match {
      case JString(value) ⇒ pattern.parseDateTime(value)
      case x ⇒ throw new MappingException("Can't convert " + x + " to DateTime")
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case x: DateTime ⇒ JString(pattern.print(x))
  }
}


object Json {
  import net.liftweb.json.Serialization._
  import net.liftweb.json._

  private val probateFormats = Serialization.formats(NoTypeHints) + new DateTimeSerializer

  //TODO: we don't technically need this in the api
  def deserialiseProbateState(json: String) = {
    implicit val formats = probateFormats
    parse(json).extract[ProbateState]
  }

  def deserialiseIncidentsState(json: String) = {
    implicit val formats = probateFormats
    parse(json).extract[IncidentsState]
  }

  def serialise(response: ProbateState) = {
    implicit val formats = probateFormats
    JsonParser.parse(write(response))
  }
  
  def serialise(response: IncidentsState) = {
    implicit val formats = probateFormats
    JsonParser.parse(write(response))
  }
}
