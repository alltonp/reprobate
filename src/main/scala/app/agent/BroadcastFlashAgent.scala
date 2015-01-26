package app.agent

import app.DateFormatForHumans
import im.mange.jetboot.{R, Renderable, Css}
import Css._
import im.mange.jetboot.Html._
import im.mange.jetboot.js.JsCmdFactory._
import java.util.UUID
import net.liftweb.http.js.jquery.JqJsCmds.FadeOut
import app.model.Broadcast
import scala.xml.Unparsed

case class BroadcastFlashAgent() extends Renderable {
  private val body = div(id = "broadcastFlashBody").styles(float(left))
  private val panel = span(body)

  def render = panel.render

  def onBroadcast(message: Broadcast) = {
    import net.liftweb.util.Helpers._
    val id = UUID.randomUUID().toString
    appendElement(body.id, broadcast(id, message)) & FadeOut(id, message.duration millis, 1 second)
  }

  private def broadcast(id: String, broadcast: Broadcast) =
    div(id = id, R(<p><strong>{DateFormatForHumans.format(broadcast.when)}</strong><br/>{Unparsed(broadcast.messages.map("- " + _).mkString("<br/>"))}</p>)).classes("alert", "alert-info").render
}
