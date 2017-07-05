package app.agent

import server.ServiceFactory._
import im.mange.jetboot.widget.Spacer
import im.mange.jetboot._
import im.mange.jetpac._
import app.server.AllRunsStatusUpdate
import java.text.DecimalFormat

case class SummaryAgent() extends Renderable {
  private val formatter = new DecimalFormat( "#,###,###,###" )
  private val body = div(id = Some("summary")).styles(display(inlineBlock))
  private val panel = div(body).classes(textCenter, "center-block").styles(marginBottom("10px"))

  def render = panel.render

  def onAllRunsStatusUpdate(update: AllRunsStatusUpdate) = body.fill(
    div(
      span(R(<small>checks executed</small>), Spacer(), R(formatter.format(update.totalExecuted))).classes("h4").styles(padding("40px")),
      span(R(<small>incidents reported</small>), Spacer(), R(formatter.format(update.totalIncidents))).classes("h4").styles(padding("40px")),
      span(R(<small>open incidents</small>), Spacer(), R(formatter.format(update.openIncidents.size))).classes("h4").styles(padding("40px")),
      span(R(<small>last updated</small>), Spacer(), R(dateFormats().standardTimeFormat.print(systemClock().dateTime))).classes("h4").styles(padding("40px"))
    )
  )
}
