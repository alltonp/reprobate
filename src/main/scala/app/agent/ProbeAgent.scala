package app.agent

import app.model.Probe
import app.server.{ProbeFailure, ProbeStatusUpdate}
import im.mange.jetboot.Renderable
import im.mange.jetboot.widget.Spacer
//import im.mange.jetboot.widget.bootstrap.{Badge, Panel}

case class ProbeAgent(probe: Probe) extends Renderable {
  import im.mange.jetboot.Css._
  import im.mange.jetboot.Html._
  import im.mange.jetboot.bootstrap3.Bootstrap._

  private val content = div(id = probe.id)

  def render = content.render

  def onProbeSuccess(update: ProbeStatusUpdate) = content.empty

  def onProbeFailed(failed: ProbeFailure) = {
    val panel = div(
      topRow(failed),
      middleRow(failed),
      bottomRow(failed)
    ).classes(alert, alertDanger).styles(margin("5px"), color("#ffffff"), backgroundColor(probe.defcon.backgroundColour), padding("10px"), paddingTop("12px"), paddingBottom("26px"))
    //TIP: stop bottom truncation ...

    content.fill(div(panel).classes("shake", probe.defcon.shake, "shake-constant", "hover-stop").styles(float(left)/*, padding("5px")*/))
  }

  private def topRow(failed: ProbeFailure) = div(description.classes(pullLeft)).styles(clear(both), marginTop("0px")).classes("h" + probe.defcon.level)
  private def middleRow(failed: ProbeFailure) = div(failures(failed).classes(pullLeft), ref(failed).classes(pullRight)).styles(clear(both), paddingTop("7px"), paddingBottom("7px"))
  private def bottomRow(failed: ProbeFailure) = div(remedy.classes(pullLeft)).styles(clear(both))

  private def description = div(span(probe.env).classes("lozenge").styles(color("#cc0000")), Spacer(), span(probe.description)).styles(fontWeight(bold))
  private def failures(failed: ProbeFailure) = span(failed.failures.head + "... (see incident log for details)").styles(fontSize(smaller))
  private def ref(failed: ProbeFailure) = span(/*Badge(value = "I123")*/).styles(fontSize(smaller))
  private def remedy = span(probe.remedy).styles(fontWeight(bold))
}
