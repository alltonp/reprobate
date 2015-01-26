package app.agent

import im.mange.jetboot.widget.Spacer
import im.mange.jetboot.{Composite, R, Renderable}
import im.mange.jetboot.Html._
import im.mange.jetboot.Css._
import app.server.AllRunsStatusUpdate
import java.text.DecimalFormat
import im.mange.jetboot.bootstrap3.Bootstrap._

case class ChecksSummaryAgent() extends Renderable {
  private val formatter = new DecimalFormat( "#,###,###,###" )
  private val body = div(id = "checksSummary").styles(display(inlineBlock))
  private val panel = div(body).classes(textCenter, "center-block").styles(marginBottom("10px"))

  def render = panel.render

  def onAllRunsStatusUpdate(update: AllRunsStatusUpdate) = body.fill(Composite(
    div(
      span(R(<small>checks executed</small>), Spacer(), R(formatter.format(update.totalExecuted))).classes("h3").styles(padding("40px")),
      span(R(<small>incidents reported</small>), Spacer(), R(formatter.format(update.totalIncidents))).classes("h3").styles(padding("40px")),
      span(R(<small>open incidents</small>), Spacer(), R(formatter.format(update.openIncidents.size))).classes("h3").styles(padding("40px"))
    )
  ))
}
