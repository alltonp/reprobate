package app.restlike.rem

import app.restlike.common.{Tracker, Script}
import net.liftweb.common.Full
import net.liftweb.http.rest.RestHelper
import net.liftweb.http._

object Rem extends RestHelper {
  import app.restlike.common.Responder._

  val appName = "rem"

  serve {
    case r@Req(`appName` :: "install" :: Nil, _, GetRequest) ⇒ () ⇒ t(Script.install(appName), downcase = false)
    case r@Req(`appName` :: "tracking" :: Nil, _, GetRequest) ⇒ () ⇒ t(Tracker(s"${appName}.tracking").view, downcase = false)
    case r@Req(`appName` :: "state" :: Nil, _, GetRequest) ⇒ () ⇒ Full(JsonResponse(Json.serialise(Persistence.load)))
    case r@Req(`appName` :: who :: Nil, _, PostRequest) ⇒ () ⇒ Controller.process(who, r)
  }
}
