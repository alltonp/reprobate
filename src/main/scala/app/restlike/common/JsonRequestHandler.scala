package app.restlike.common

import net.liftweb.common.{Box, Full, Loggable}
import net.liftweb.http.{LiftResponse, Req}
import net.liftweb.json.JsonAST

object JsonRequestHandler extends Loggable {
  import Responder._

  def handle(req: Req)(process: (JsonAST.JValue, Req) ⇒ Box[LiftResponse]) = {
    try {
      req.json match {
        case Full(json) ⇒ process(json, req)
        case o ⇒ println(req.json); t(List(s"unexpected item in the bagging area ${o}"))
      }
    } catch {
      case e: Exception ⇒ println("### Error handling request: " + req + " - " + e.getMessage); t(List(e.getMessage))
    }
  }
}
