package app.agent

import app.DateFormatForHumans
import im.mange.jetboot._
import Css._
import Js._
import Html._
import java.util.UUID
import net.liftweb.http.js.jquery.JqJsCmds.FadeOut
import app.model.Broadcast
import scala.xml.Unparsed

case class BroadcastFlashAgent() extends Renderable {
  private val body = div(id = Some("broadcastFlash")).styles(float(left))
  private val panel = span(body)

  def render = panel.render

  def onBroadcast(message: Broadcast) = {
    import net.liftweb.util.Helpers._
    val id = UUID.randomUUID().toString
    appendElement(body.id, broadcast(id, message)) & FadeOut(id, message.duration millis, 1 second)
  }

  private def broadcast(id: String, broadcast: Broadcast) =
    div(id = Some(id), R(<p><strong>{DateFormatForHumans.format(broadcast.when)}</strong><br/>{Unparsed(broadcast.messages.map("- " + _).mkString("<br/>"))}</p>)).classes("alert", "alert-info").render
}
