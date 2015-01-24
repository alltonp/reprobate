package im.mange.reprobate.api

import im.mange.shoreditch.api.{Check, CheckResponse}
import net.liftweb.common.Full
import net.liftweb.http.JsonResponse

object Runner {
  def run(p: Check) = {
    val r = try { p.run }
    catch { case e: Throwable ⇒ CheckResponse(List(e.getMessage)) }
    Full(JsonResponse(Json.serialise(r)))
  }
}

object Json {
  import net.liftweb.json.Serialization._
  import net.liftweb.json._

  private val probeFormats = Serialization.formats(NoTypeHints)

  //TODO: we don't techincally need this in the api
  def deserialise(json: String) = {
    implicit val formats = probeFormats
    parse(json).extract[CheckResponse]
  }

  def serialise(response: CheckResponse) = {
    implicit val formats = probeFormats
    JsonParser.parse(write(response))
  }
}

