package app.restlike.common

import net.liftweb.common.Full
import net.liftweb.http.PlainTextResponse

object Responder {
  def t(messages: List[String], downcase: Boolean = false) = {
    val response = messages.mkString("\n")
    //    println("<= " + response)
    Full(PlainTextResponse(if (downcase) response.toLowerCase else response))
  }
}
