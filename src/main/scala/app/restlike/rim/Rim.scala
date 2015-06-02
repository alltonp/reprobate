package app.restlike.rim

import app.restlike.common.{Tracker, Script}
import net.liftweb.common.Full
import net.liftweb.http.rest.RestHelper
import net.liftweb.http._

object Rim extends RestHelper {
  import app.restlike.common.Responder._

  //TODO: extract app name
  serve {
    case r@Req("rim" :: "install" :: Nil, _, GetRequest) ⇒ () ⇒ t(Script.install("rim"), downcase = false)
    case r@Req("rim" :: "tracking" :: Nil, _, GetRequest) ⇒ () ⇒ t(Tracker("rim.tracking").view, downcase = false)
    case r@Req("rim" :: "state" :: Nil, _, GetRequest) ⇒ () ⇒ Full(JsonResponse(Json.serialise(Persistence.load)))
    case r@Req("rim" :: who :: Nil, _, PostRequest) ⇒ () ⇒ Controller.process(who, r)
  }
}
