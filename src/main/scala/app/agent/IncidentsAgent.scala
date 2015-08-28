package app.agent

import app.ServiceFactory.dateFormats
import app.model.Incident
import app.server.AllRunsStatusUpdate
import im.mange.jetboot._
import im.mange.jetpac._
import im.mange.jetboot.widget.table.{TableHeaders, TableRow}

import scala.xml.Unparsed

//TODO: mouse over to see incident details ... start/finish etc
case class IncidentsAgent() extends Renderable {
  private val body = div(id = Some("incidentsBody")).styles(fontSize(smaller))
  private val panel = div(body).styles(marginTop("15px"))

  def render = panel.render

  def onAllRunsStatusUpdate(update: AllRunsStatusUpdate) =
    if (update.totalIncidents == 0) body.empty else body.fill(layout(update))

  private def layout(update: AllRunsStatusUpdate) = Bs.row(col(12, render(update.openIncidents, update.closedIncidents)))

  //TODO: pull out presentation
  private def render(open: List[Incident], closed: List[Incident]) = {
    R(
      if (!open.isEmpty) tablify(tableHeaders("Open Incidents: " + open.size), rows(open)) else R(),
      if (!closed.isEmpty) tablify(tableHeaders("Closed Incidents: " + closed.size), rows(closed)) else R())
  }

  //TODO: ideally lets give 1 and 3 more room
  private def tableHeaders(prefix: String) = headers(List(
    header(span(None, prefix).styles(color("#0088cc"))).styles(width("25%")),
    header(R("Environment")).styles(width("9%")),
//    header(R("Ref")).styles(width("7%")),
    header(R("Reason")).styles(width("32%")),
    header(R("Opened")).styles(width("12%")),
    header(R("Closed")).styles(width("12%")),
    header(R("Duration")).styles(width("10%"))
  ))

  private def rows(incidents: List[Incident]) = incidents.map(i => TableRow(None, List(
    R(i.probe.description),
    R(i.probe.env),
//    R(s"IN-${i.id}"),
    R(Unparsed(i.failures.mkString("<br/>"))),
    R(dateFormats().today(i.start)),
    R(i.finish.fold("-"){dateFormats().today(_)}),
    R(dateFormats().ago(i.openDuration))
  )))

  //TODO: pull out a widget
  private def tablify(h: TableHeaders, r: List[TableRow]) =
    div(None, bsTable(h, r).classes(tableCondensed, tableStriped).styles(marginBottom("0px"))).classes("round-corners")
}
