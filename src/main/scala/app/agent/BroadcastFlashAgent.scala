package app.agent

import app.DateFormatForHumans
import app.server.ProbeFailure
import im.mange.jetboot._
import Css._
import Js._
import im.mange.jetboot.Html._
import java.util.UUID
import im.mange.jetboot.bootstrap3.Bootstrap._
import im.mange.jetboot.widget.Spacer
import net.liftweb.http.js.jquery.JqJsCmds.FadeOut
import app.model.Broadcast
import scala.xml.Unparsed

case class BroadcastFlashAgent() extends Renderable {
  private val body = div(id = Some("broadcastFlash"))//.styles(float(left))
  private val panel = span(body)

  def render = panel.render

  def onBroadcast(message: Broadcast) = {
    import net.liftweb.util.Helpers._
    val id = UUID.randomUUID().toString
    appendElement(body.id, broadcast(id, message)) & FadeOut(id, message.durationSeconds seconds, 1 second)
  }

  private def broadcast(id: String, broadcast: Broadcast) = {
    div(id = Some(id),
      topRow(broadcast),
//      middleRow(broadcast)//,
      bottomRow(broadcast)
    )
    .classes(alert, "alert-info")
    .styles(float(left), margin("5px"), padding("10px")/*, paddingTop("12px"), paddingBottom("26px")*/)
    .render
  }

  //TODO: show: 11:27:50 to 11:29:50 Today
  private def topRow(broadcast: Broadcast) =
    div(None, description(broadcast).classes(pullLeft)).styles(clear(both), marginTop("0px"))

  private def middleRow(broadcast: Broadcast) =
    div(None, failures(broadcast).classes(pullLeft), ref(broadcast).classes(pullRight)).styles(clear(both), paddingTop("7px"), paddingBottom("7px"))

  private def bottomRow(broadcast: Broadcast) = div(remedy(broadcast).classes(pullLeft)).styles(clear(both))

  private def description(broadcast: Broadcast) = div(None,
    span(None, broadcast.env).classes("lozenge").styles(color("#cc0000")), Spacer(),
    span(None, DateFormatForHumans.format(broadcast.when)).styles(fontSize(smaller))
  ).styles(fontWeight(bold))

  private def failures(broadcast: Broadcast) = span(None, broadcast.messages.head + "").styles(fontSize(smaller))
  private def ref(broadcast: Broadcast) = span(/*Badge(value = "I123")*/).styles(fontSize(smaller))
  private def remedy(broadcast: Broadcast) = span(None, broadcast.messages.head).styles(fontWeight(bold))

}
