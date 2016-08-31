package app.restlike.dogfood

import im.mange.reprobate.api.Runner
import im.mange.shoreditch.api.Check
import im.mange.shoreditch.check.Alive
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.{GetRequest, PlainTextResponse, Req}
import app.ServiceFactory
import net.liftweb.common.{Box, Failure, Full}
import app.model.FailedProbe

object Blink1 extends RestHelper {
  serve {
    case Req("blink1" :: "status" :: Nil, _, GetRequest) ⇒ () ⇒ doIt("")
    case Req("blink1" :: "status" :: env :: Nil, _, GetRequest) ⇒ () ⇒ doIt(env)
  }

  private def doIt(env: String): Full[PlainTextResponse] =
    Full(PlainTextResponse(if (OkProbe(env).run.failures.isEmpty) "#00FF00" else "#FF0000"))
}

//TODO: use new RestHelper
object Dogfood extends RestHelper {
  serve {
    //TODO: don't like this name much ... think of something better (reprobate in the name?)
    case Req("check" :: "probes" :: "ok" :: Nil, _, GetRequest) ⇒ () ⇒ { Runner.run(OkProbe("")) }
    case Req("check" :: "probes" :: "ok" :: env :: Nil, _, GetRequest) ⇒ () ⇒ { Runner.run(OkProbe(env)) }
    case Req("check" :: "alive" :: Nil, _, GetRequest) ⇒ () ⇒ { Runner.run(Alive) }
  }
}

case class OkProbe(env: String) extends Check {
  def run = {
    val future = ServiceFactory.probeProviderActor() !< GetProbeStatuses
    val result = future.get(60 * 1000).asInstanceOf[Box[ProbeStatuses]]

    result match {
      case Full(x) if x.failures.isEmpty => success
      case Full(x) => {
        val probesMatchingEnv = x.failures.filter(p => env.isEmpty || env == "index" || p.probe.env.toLowerCase.contains(env.toLowerCase))
//        println(env + "=" + probesMatchingEnv)
        if (probesMatchingEnv.isEmpty) success else failure(probesMatchingEnv.map(fp => fp.probe.name + ": " + fp.failures.head).toList)
      }
      case Failure(f, _, _) => failure(List(f))
      case _ => failure(List("A problem occurred getting probe status"))
    }
  }
}

case object GetProbeStatuses

case class ProbeStatuses(failures: Iterable[FailedProbe])