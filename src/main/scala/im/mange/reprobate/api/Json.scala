package im.mange.reprobate.api

object Json {
  import net.liftweb.json.Serialization._
  import net.liftweb.json._

  private val probeFormats = Serialization.formats(NoTypeHints)

  //TODO: we don't techincally need this in the api
  def deserialise(json: String) = {
    implicit val formats = probeFormats
    parse(json).extract[ProbeResponse]
  }

  def serialise(response: ProbeResponse) = {
    implicit val formats = probeFormats
    JsonParser.parse(write(response))
  }
}
