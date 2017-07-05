package app.agent

import server.ServiceFactory.dateFormats
import app.server.ProbeFailure
import im.mange.jetboot.Bss
import im.mange.jetpac._
import java.util.UUID
import im.mange.jetboot.widget.Spacer
import net.liftweb.http.js.jquery.JqJsCmds.FadeOut
import app.model.Broadcast
import scala.xml.Unparsed
import Bss._

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

  private def topRow(broadcast: Broadcast) =
    div(None, description(broadcast).classes(pullLeft)).styles(clear(both), marginTop("0px"))

  private def middleRow(broadcast: Broadcast) =
    div(None, failures(broadcast).classes(pullLeft), ref(broadcast).classes(pullRight)).styles(clear(both), paddingTop("7px"), paddingBottom("7px"))

  private def bottomRow(broadcast: Broadcast) = div(message(broadcast).classes(pullLeft)).styles(clear(both))

  private def description(broadcast: Broadcast) = div(None,
    span(None, broadcast.env).classes("lozenge").styles(color("#cc0000")), Spacer(),
    span(None, dateFormats().standardTimeFormat.print(broadcast.start) + " to " + dateFormats().today(broadcast.finish)).styles(fontSize(smaller))
  ).styles(fontWeight(bold))

  private def failures(broadcast: Broadcast) = span(None, broadcast.messages.head + "").styles(fontSize(smaller))
  private def ref(broadcast: Broadcast) = span(/*Badge(value = "I123")*/).styles(fontSize(smaller))

  //TODO: should probably head option this to avoid blowing up when clients send rubbish ...
  private def message(broadcast: Broadcast) =
    span(None, Strings.sentenceCase(broadcast.messages.head)).styles(fontWeight(bold))
}

object Strings {
  def sentenceCase(value: String) = value.head.toUpper + value.drop(1)
}
