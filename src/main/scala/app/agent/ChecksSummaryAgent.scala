package app.agent

import app.server.ProbeSummaryResponse
import app.ui.BigSpinner
import im.mange.jetboot._
import im.mange.jetpac._

case class ChecksSummaryAgent() extends Renderable {
  private val holder = div(Some("checksSummary")).classes("hidden").styles(marginTop("5px"))

  def render = holder.render
  def requestSummary = holder.show & holder.fill(BigSpinner("checksSummarySpinner", "Loading checks summary..."))
  def show(response: ProbeSummaryResponse) = holder.fill(ChecksConfigPresentation(response.probes))
  def hide = holder.empty & holder.hide
}