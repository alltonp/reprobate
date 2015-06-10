package app.restlike.rem

object Json {
  import net.liftweb.json.Serialization._
  import net.liftweb.json._

  private val theFormats = Serialization.formats(NoTypeHints)

  def deserialise(json: String) = {
    implicit val formats = theFormats
    parse(json).extract[Universe]
  }

  def serialise(response: Universe) = {
    implicit val formats = theFormats
    JsonParser.parse(write(response))
  }

  def serialise(response: Option[Model]) = {
    implicit val formats = theFormats
    JsonParser.parse(write(response))
  }
}
