package app.restlike.rim

import app.restlike.common.{Script, Tracker}
import net.liftweb.common.Full
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper
import org.json4s.native.JsonMethods

object Rim extends RestHelper {
  import app.restlike.common.Responder._

  val appName = "rim"

  serve {
    case r@Req(`appName` :: "install" :: Nil, _, GetRequest) ⇒ () ⇒ t(Script.install(appName), downcase = false)
    case r@Req(`appName` :: "tracking" :: Nil, _, GetRequest) ⇒ () ⇒ t(Tracker(s"$appName.tracking").view, downcase = false)
    case r@Req(`appName` :: "state" :: Nil, _, GetRequest) ⇒ () ⇒
      Full(PlainTextResponse(JsonMethods.pretty(JsonMethods.render(Json.serialise(Persistence.load)))))
    case r@Req(`appName` :: who :: Nil, _, PostRequest) ⇒ () ⇒ Controller.process(who, r)
  }
}
