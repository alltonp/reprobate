package app.restlike.dogfood

import im.mange.reprobate.api.Runner
import im.mange.shoreditch.api.Check
import im.mange.shoreditch.check.Alive
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.{GetRequest, JsonResponse, PlainTextResponse, Req}
import net.liftweb.common.{Box, Empty, Failure, Full}
import app.model._
import app.server.ProbeStatus
import net.liftweb.actor.LAFuture
import net.liftweb.json._
import server.ServiceFactory
import server.tea.{Model, State}

object Blink1 extends RestHelper {
  serve {
    case Req("blink1" :: "status" :: Nil, _, GetRequest) ⇒ () ⇒ doIt("")
    case Req("blink1" :: "status" :: env :: Nil, _, GetRequest) ⇒ () ⇒ doIt(env)
    case Req("blink1" :: "pattern" :: Nil, _, GetRequest) ⇒ () ⇒ doItPattern("")
    case Req("blink1" :: "pattern" :: env :: Nil, _, GetRequest) ⇒ () ⇒ doItPattern(env)
  }

  //TODO: use: 0000FF for no probes setup (or broadcasts)
  //TODO: use: E77B00 for has some inactive (maybe)
  private def doIt(env: String): Full[PlainTextResponse] = {
    Full(PlainTextResponse(if (OkProbe(env).run.failures.isEmpty) "#00FF00" else "#FF0000"))
  }

  //TODO: handle inAReleaseWindow
  private def doItPattern(env: String): Full[PlainTextResponse] = {
    Full(PlainTextResponse(if (OkProbe(env).run.failures.isEmpty) "pattern: pass" else "pattern: fail"))
  }
}

//TODO: use new RestHelper
object Dogfood extends RestHelper {
  serve {
    //TODO: don't like this name much ... think of something better (reprobate in the name?)
    case Req("check" :: "all" :: "ok" :: Nil, _, GetRequest) ⇒ () ⇒ { Runner.run(OkProbe("")) }
    case Req("check" :: "all" :: "ok" :: env :: Nil, _, GetRequest) ⇒ () ⇒ { Runner.run(OkProbe(env)) }
    case Req("check" :: "alive" :: Nil, _, GetRequest) ⇒ () ⇒ { Runner.run(Alive) }

    //TODO: I'm not really dogfood, because I'm not a check ...
    case Req("incidents" :: Nil, _, GetRequest) ⇒ () ⇒ { Full(JsonResponse(Json.serialise(IncidentRegistry.load))) }


    case Req("state" :: Nil, _, GetRequest) ⇒ () ⇒ {
      val future = ServiceFactory.update() !< GetState
      awaitAndRespond[State](future, Json.serialise)
    }

    case Req("state" :: "probes" :: Nil, _, GetRequest) ⇒ () ⇒ {
      val future = ServiceFactory.update() !< GetAllProbeStatuses
      awaitAndRespond[List[(Probe, Option[ProbeStatus])]](future, Json.serialise)
    }
  }

  private def awaitAndRespond[T](future: LAFuture[Any], serialise: T => JValue) = {
    val result = future.get(60 * 1000).asInstanceOf[Box[T]]
    result match {
      case Full(x)          => Full(JsonResponse(serialise(x)))
      case Failure(f, _, _) => Full(PlainTextResponse(f, 500))
      case Empty            => Full(PlainTextResponse("Timed out", 500))
      case _                => Full(PlainTextResponse(s"Bug: Did not expect: $result", 500))
    }
  }
}

case class OkProbe(env: String) extends Check {
  def run = {
    val future = ServiceFactory.update() !< GetProbeStatuses
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
case object GetState
case object GetAllProbeStatuses

case class ProbeStatuses(failures: Iterable[FailedProbe])
