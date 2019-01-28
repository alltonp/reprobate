package app.restlike.rim

import app.restlike.common.{History, Script, Tracker}
import net.liftweb.common.Full
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper
import org.json4s.native.JsonMethods
import server.ServiceFactory

object Rim extends RestHelper {
  import app.restlike.common.Responder._

  val appName = "rim"

//  println(java.util.UUID.randomUUID.toString)

  serve {
    case r@Req(`appName` :: "install" :: token :: Nil, _, GetRequest) ⇒ () ⇒
      t(Script.install(appName, token), downcase = false)
    //TODO: this should be filtered for the token
    case r@Req(`appName` :: "tracking" :: token :: Nil, _, GetRequest) ⇒ () ⇒
      t(history(token).map(_.printable).toList, downcase = false)
    case r@Req(`appName` :: "state" :: token :: Nil, _, GetRequest) ⇒ () ⇒
      Full(PlainTextResponse(JsonMethods.pretty(JsonMethods.render(Json.serialise(Persistence.load.modelForCli(token))))))
    case r@Req(`appName` :: who :: token :: Nil, _, PostRequest) ⇒ () ⇒
      Controller.process(who, r, token)
  }

  def history(token: String) = Tracker(s"${ServiceFactory.dataDir}/$appName.tracking").view(token)
}
