package app.agent

import jetboot.{Composite, R, Renderable}
import jetboot.widget.SimpleWidgets._
import jetboot.Css._
import app.server.AllRunsStatusUpdate
import java.text.DecimalFormat
import jetboot3.Bootstrap._

case class ProbeSummaryAgent() extends Renderable {
  private val formatter = new DecimalFormat( "#,###,###,###" )
  private val body = div(id = "probeSummaryBody").styles(display(inlineBlock))
  private val panel = div(body).classes(textCenter, "center-block").styles(marginBottom("10px"))

  def render = panel.render

  //TODO: each thing could be a widget here
  def onAllRunsStatusUpdate(update: AllRunsStatusUpdate) = body.fill(Composite(
    div(
      span(R(<small>checks executed</small>), Spacer(), R(formatter.format(update.totalExecuted))).classes("h3").styles(padding("40px")),
      span(R(<small>incidents reported</small>), Spacer(), R(formatter.format(update.totalIncidents))).classes("h3").styles(padding("40px")),
      span(R(<small>open incidents</small>), Spacer(), R(formatter.format(update.openIncidents.size))).classes("h3").styles(padding("40px"))
    )
  ))
}
