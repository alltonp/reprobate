package app.agent

import app.server.ProbeConfigResponse
import app.ui.BigSpinner
import im.mange.jetboot._
import im.mange.jetpac._

case class ChecksConfigAgent() extends Renderable {
  private val holder = div(Some("checksConfig")).classes("hidden").styles(marginTop("5px"))

  def render = holder.render
  def requestConfig = holder.show & holder.fill(BigSpinner("checksConfigSpinner", "Loading checks..."))
  def show(response: ProbeConfigResponse) = holder.fill(ChecksConfigPresentation(response.probes))
  def hide = holder.empty & holder.hide
}