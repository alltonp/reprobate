package im.mange.reprobate.api

import net.liftweb.common.Full
import net.liftweb.http.JsonResponse

object Runner {
  import ProbeResponse._

  def run(p: Probe) = {
    val r = try { p.probe }
    catch { case e: Throwable ⇒ epicFail(List(e.getMessage)) }
    Full(JsonResponse(Json.serialise(r)))
  }
}
