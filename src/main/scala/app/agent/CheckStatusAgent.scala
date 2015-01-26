package app.agent

import app.model.Probe
import app.server.{ProbeFailure, ProbeStatusUpdate}
import im.mange.jetboot.Renderable
import im.mange.jetboot.widget.Spacer

case class CheckStatusAgent(probe: Probe) extends Renderable {
  import im.mange.jetboot.Css._
  import im.mange.jetboot.Html._
  import im.mange.jetboot.bootstrap3.Bootstrap._

  private val content = div(id = probe.id)

  def render = content.render

  def onSuccess(update: ProbeStatusUpdate) = content.empty

  def onFailure(failure: ProbeFailure) = {
    val panel = div(
      topRow(failure),
      middleRow(failure),
      bottomRow(failure)
    ).classes(alert, alertDanger).styles(margin("5px"), color("#ffffff"), backgroundColor(probe.defcon.backgroundColour), padding("10px"), paddingTop("12px"), paddingBottom("26px"))
    //TIP: stop bottom truncation ...

    content.fill(div(panel).classes("shake", probe.defcon.shake, "shake-constant", "hover-stop").styles(float(left)))
  }

  private def topRow(failure: ProbeFailure) =
    div(description.classes(pullLeft)).styles(clear(both), marginTop("0px")).classes("h" + probe.defcon.level)

  private def middleRow(failure: ProbeFailure) =
    div(failures(failure).classes(pullLeft), ref(failure).classes(pullRight)).styles(clear(both), paddingTop("7px"), paddingBottom("7px"))

  private def bottomRow(failure: ProbeFailure) = div(remedy.classes(pullLeft)).styles(clear(both))

  private def description = div(span(probe.env).classes("lozenge").styles(color("#cc0000")), Spacer(), span(probe.description)).styles(fontWeight(bold))
  private def failures(failure: ProbeFailure) = span(failure.failures.head + "... (see incident log for details)").styles(fontSize(smaller))
  private def ref(failure: ProbeFailure) = span(/*Badge(value = "I123")*/).styles(fontSize(smaller))
  private def remedy = span(probe.remedy).styles(fontWeight(bold))
}
