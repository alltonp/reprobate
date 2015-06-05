package app.restlike.rim

import im.mange.little.json.LittleSerialisers

object Json {
  import net.liftweb.json.Serialization._
  import net.liftweb.json._

  private val theFormats = Serialization.formats(NoTypeHints)

  def deserialise(json: String) = {
    implicit val formats = theFormats
    parse(json).extract[Model]
  }

  def serialise(response: Model) = {
    implicit val formats = theFormats
    JsonParser.parse(write(response))
  }
}
