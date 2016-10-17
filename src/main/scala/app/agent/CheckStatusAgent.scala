package app.agent

import app.model.Probe
import app.server.{ProbeFailure, ProbeStatusUpdate}
import im.mange.jetboot._
import im.mange.jetpac._
import im.mange.jetboot.widget.Spacer

case class CheckStatusAgent(probe: Probe) extends Renderable {
  private val content = div(id = Some("checkStatus_" + probe.id))

  def render = content.render

  def onSuccess(update: ProbeStatusUpdate) = content.empty

  def onFailure(failure: ProbeFailure) = {
    val panel = div(None,
      topRow(failure),
      middleRow(failure),
      bottomRow(failure)
    ).classes(alert, alertDanger).styles(margin("5px"), color("#ffffff"), backgroundColor(probe.defcon.backgroundColour), padding("10px"), paddingTop("12px"), paddingBottom("26px"))
    //TIP: stop bottom truncation ...

    content.fill(div(panel).classes("shake", probe.defcon.shake, "shake-constant", "hover-stop").styles(float(left)))
  }

  private def topRow(failure: ProbeFailure) =
    div(None, description.classes(pullLeft)).styles(clear(both), marginTop("0px")).classes("h" + probe.defcon.level)

  private def middleRow(failure: ProbeFailure) =
    div(None, failures(failure).classes(pullLeft), ref(failure).classes(pullRight)).styles(clear(both), paddingTop("7px"), paddingBottom("7px"))

  private def bottomRow(failure: ProbeFailure) = div(remedy.classes(pullLeft)).styles(clear(both))

  private def description = div(None, span(None, probe.env).classes("lozenge").styles(color("#cc0000")), Spacer(),
    span(None, probe.description)).styles(fontWeight(bold))

  private def failures(failure: ProbeFailure) = {
    val message = failure.failures.head
    span(None, (if (message.size > 100) message.substring(0, 99) + "... " else message) + " (see incident log for details)").styles(fontSize(smaller))
  }

  private def ref(failure: ProbeFailure) = span(/*Badge(value = "I123")*/).styles(fontSize(smaller))
  private def remedy = span(None, probe.remedy).styles(fontWeight(bold))
}
