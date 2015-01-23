package app.restlike.dogfood

import im.mange.reprobate.api.{Probe, Runner}
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.{GetRequest, Req}
import app.ServiceFactory
import net.liftweb.common.{Box, Full, Failure}
import app.model.FailedProbe

//TODO: use new RestHelper
object Dogfood extends RestHelper {
  serve {
    case Req("check" :: "probes" :: "ok" :: env :: Nil, _, GetRequest) ⇒ () ⇒ { Runner.run(OkProbe(env)) }
  }
}

case class OkProbe(env: String) extends Probe {
  import im.mange.reprobate.api.ProbeResponse._

  def probe = {
    val future = ServiceFactory.probeProviderActor() !< GetProbeStatuses
    val result = future.get(60 * 1000).asInstanceOf[Box[ProbeStatuses]]

    result match {
      case Full(x) if x.failures.isEmpty => win
      case Full(x) => {
        val probesMatchingEnv = x.failures.filter(p => env.isEmpty || p.probe.env.toLowerCase == env.toLowerCase)
        if (probesMatchingEnv.isEmpty) win else epicFail(probesMatchingEnv.map(fp => fp.probe.name + ": " + fp.failures.head).toList)
      }
      case Failure(f, _, _) => epicFail(List(f))
      case _ => epicFail(List("A problem occurred getting probe status"))
    }
  }
}

case object GetProbeStatuses

case class ProbeStatuses(failures: Iterable[FailedProbe])