package app.restlike.rim

import net.liftweb.common.Full
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.{GetRequest, PlainTextResponse, PostRequest, Req}

object Rim extends RestHelper {
  import app.restlike.rim.Messages._
  import app.restlike.rim.Responder._

  serve {
    case r@Req("rim" :: "install" :: Nil, _, GetRequest) ⇒ () ⇒ t(install, downcase = false)
    case r@Req("rim" :: who :: Nil, _, PostRequest) ⇒ () ⇒ Controller.process(who, r)
  }
}

object Responder {
  def t(messages: List[String], downcase: Boolean = false) = {
    val response = messages.mkString("\n")
//    println("<= " + response)
    Full(PlainTextResponse(if (downcase) response.toLowerCase else response))
  }
}
