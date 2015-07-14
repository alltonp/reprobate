package app.restlike.gtd

import app.restlike.common.{Script, Tracker}
import net.liftweb.common.Full
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper
import org.json4s.native.JsonMethods

object Gtd extends RestHelper {
  import app.restlike.common.Responder._

  val appName = "gtd"

  serve {
    case r@Req(`appName` :: "install" :: token :: Nil, _, GetRequest) ⇒ () ⇒
      t(Script.install(appName, token), downcase = false)
    //TODO: this should be filtered for the token
    case r@Req(`appName` :: "tracking" :: token :: Nil, _, GetRequest) ⇒ () ⇒
      t(Tracker(s"$appName.tracking").view, downcase = false)
    case r@Req(`appName` :: "state" :: token :: Nil, _, GetRequest) ⇒ () ⇒
      Full(PlainTextResponse(JsonMethods.pretty(JsonMethods.render(Json.serialise(Persistence.load.modelFor(token))))))
    case r@Req(`appName` :: who :: token :: Nil, _, PostRequest) ⇒ () ⇒
      Controller.process(who, r, token)
  }
}
