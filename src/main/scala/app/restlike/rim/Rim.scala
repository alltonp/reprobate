package app.restlike.rim

import app.restlike.common.{Tracker, Script}
import net.liftweb.common.Full
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper
import org.json4s.JValue
import org.json4s.native.JsonMethods

//import net.liftweb.http._

object Rim extends RestHelper {
  import app.restlike.common.Responder._

  val appName = "rim"

  serve {
    case r@Req(`appName` :: "install" :: Nil, _, GetRequest) ⇒ () ⇒ t(Script.install(appName), downcase = false)
    case r@Req(`appName` :: "tracking" :: Nil, _, GetRequest) ⇒ () ⇒ t(Tracker(s"$appName.tracking").view, downcase = false)
    case r@Req(`appName` :: "state" :: Nil, _, GetRequest) ⇒ () ⇒
      val serialise: JValue = Json.serialise(Persistence.load)
      Full(PlainTextResponse(JsonMethods.pretty(JsonMethods.render(serialise))))
    case r@Req(`appName` :: who :: Nil, _, PostRequest) ⇒ () ⇒ Controller.process(who, r)
  }
}
