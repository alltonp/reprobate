package app.agent

import app.server.ProbeConfigResponse
import app.ui.BigSpinner
import im.mange.jetpac._
import im.mange.jetpac.comet.Subscriber

case class ChecksConfigAgent(subscriber: Subscriber) extends Renderable {
  private val holder = div(Some("checksConfig")).classes("hidden").styles(marginTop("5px"))

  private val configEditorAgent = ConfigEditorAgent(subscriber)

  def render = holder.render
  def requestSummary = holder.show & holder.fill(BigSpinner("checksConfigSpinner", "Loading checks config..."))
  def show(response: ProbeConfigResponse) = holder.fill(R(configEditorAgent)) & configEditorAgent.onLoad(response.config)
  def hide = holder.empty & holder.hide
}