package app.restlike.rem

import app.restlike.common.{Tracker, Script}
import net.liftweb.common.Full
import net.liftweb.http.rest.RestHelper
import net.liftweb.http._

object Rem extends RestHelper {
  import app.restlike.common.Responder._

  //TODO: extract app name
  serve {
    case r@Req("rem" :: "install" :: Nil, _, GetRequest) ⇒ () ⇒ t(Script.install("rem"), downcase = false)
    case r@Req("rem" :: "tracking" :: Nil, _, GetRequest) ⇒ () ⇒ t(Tracker("rem.tracking").view, downcase = false)
    case r@Req("rem" :: "state" :: Nil, _, GetRequest) ⇒ () ⇒ Full(JsonResponse(Json.serialise(Persistence.load)))
    case r@Req("rem" :: who :: Nil, _, PostRequest) ⇒ () ⇒ Controller.process(who, r)
  }
}
