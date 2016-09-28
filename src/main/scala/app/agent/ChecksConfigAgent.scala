package app.agent

import app.server.{ProbeConfigResponse, ProbeSummaryResponse}
import app.ui.BigSpinner
import im.mange.jetpac._

case class ChecksConfigAgent() extends Renderable {
  private val holder = div(Some("checksConfig")).classes("hidden").styles(marginTop("5px"))

  def render = holder.render
  def requestSummary = holder.show & holder.fill(BigSpinner("checksConfigSpinner", "Loading checks config..."))
  def show(response: ProbeConfigResponse) = holder.fill(R(response.config))
  def hide = holder.empty & holder.hide
}