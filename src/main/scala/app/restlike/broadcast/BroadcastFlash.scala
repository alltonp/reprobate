package app.restlike.broadcast

import app.ServiceFactory
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper
import net.liftweb.json._

object BroadcastFlash extends RestHelper {
  serve {
    case r@Req("broadcast" :: Nil, _, PostRequest) ⇒ () ⇒ { BroadcastMessage.send(r) }
  }
}

object JsonRequestHandler extends Loggable {
  def handle(req: Req)(process: (JsonAST.JValue, Req) ⇒ Box[LiftResponse]) = {
    try {
      req.json match {
        case Empty ⇒ requestFailed(req, "Request did not contain any Json")
        case Full(JNothing) ⇒ requestFailed(req, "Request Json was empty")
        case Full(json) ⇒ process(json, req)
        case Failure(m, e, c) ⇒ requestFailed(req, "Request Json was invalid: " + m)
      }
    } catch {
      case e: Exception ⇒ println("### Error handling request: " + req + " - " + e.getMessage); requestFailed(req, e.getMessage)
    }
  }

  //TODO: we need something better here, just not sure what yet ... some sort of http status code
  def requestFailed(req: Req, message: String) = Full(PlainTextResponse(message))
}

object BroadcastRequestJson {
  import net.liftweb.json._

  def deserialise(json: String) = {
    implicit val formats = Serialization.formats(NoTypeHints)
    parse(json).extract[BroadcastFlash]
  }
}

case class BroadcastFlash(messages: List[String], env: String, durationSeconds: Int)

case object BroadcastMessage {
  def send(req: Req) = {
    JsonRequestHandler.handle(req)((json, req) ⇒ {
      val broadcast = BroadcastRequestJson.deserialise(pretty(render(json)))
      ServiceFactory.update() ! broadcast.copy(env = broadcast.env.toUpperCase)
      Full(PlainTextResponse("OK"))
    })
  }
}

