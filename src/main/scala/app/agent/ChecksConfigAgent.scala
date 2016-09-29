package app.agent

import app.agent.columneditor.ColumnEditableAgent
import app.agent.configeditor.{Column, ColumnConfig, ConfigEditableAgent, ConfigEditorAgent}
import app.server.{ProbeConfigResponse, ProbeSummaryResponse}
import app.ui.BigSpinner
import im.mange.jetpac._
import im.mange.jetpac.comet.Subscriber

case class ChecksConfigAgent(subscriber: Subscriber) extends Renderable {
  private val holder = div(Some("checksConfig")).classes("hidden").styles(marginTop("5px"))

  private val configEditorAgent = ConfigEditorAgent(
    ColumnConfig(Seq(Column("one", true, false), Column("two", true, false))),
    subscriber, new ConfigEditableAgent() {
      override def onColumnsChanged: Unit = println("changed")
      override def onColumnsSaved: Unit = println("saved")
    }
  )

  def render = holder.render
  def requestSummary = holder.show & holder.fill(BigSpinner("checksConfigSpinner", "Loading checks config..."))
  def show(response: ProbeConfigResponse) = configEditorAgent.onLoad(response.config) & holder.fill(R(configEditorAgent))
  def hide = holder.empty & holder.hide
}