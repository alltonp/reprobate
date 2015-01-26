package app.agent

import app.server.ProbeConfigResponse
import im.mange.jetboot.Css._
import im.mange.jetboot.Html._
import im.mange.jetboot.Renderable

case class ChecksConfigAgent() extends Renderable {
  private val holder = div("checksConfig").classes("hidden").styles(marginTop("5px"))

  def render = holder.render
  def requestConfig = holder.show & holder.fill(BigSpinner("checksConfigSpinner", "Loading checks..."))
  def show(response: ProbeConfigResponse) = holder.fill(ProbeConfigPresentation(response.probes))
  def hide = holder.empty & holder.hide
}