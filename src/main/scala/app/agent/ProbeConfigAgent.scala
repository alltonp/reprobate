package app.agent

import jetboot.widget.SimpleWidgets._
import jetboot.Css._
import jetboot.{R, Renderable}
import app.server.ProbeConfigResponse

case class ProbeConfigAgent() extends Renderable {
  private val holder = div("probeConfig").classes("hidden").styles(marginTop("5px"))

  def render = holder.render

  def requestConfig = holder.show & holder.fill(BigSpinner("probeConfigSpinner", "Loading checks..."))

  //TODO: probably showConfig or something
  def onProbeConfigResponse(response: ProbeConfigResponse) = holder.fill(ProbeConfigPresentation(response.probes))

  def hide = holder.empty & holder.hide
}