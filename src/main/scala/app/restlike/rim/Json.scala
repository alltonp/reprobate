package app.restlike.rim

import im.mange.little.json.{LittleJodaSerialisers, LittleSerialisers}

object Json {
//  import net.liftweb.json.Serialization._
//  import net.liftweb.json._
  import org.json4s._
  import org.json4s.native.JsonMethods._
  import org.json4s.native.Serialization.write
  import org.json4s.native.{JsonParser, Serialization}

  private val theFormats = Serialization.formats(NoTypeHints) ++ LittleJodaSerialisers.all

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
