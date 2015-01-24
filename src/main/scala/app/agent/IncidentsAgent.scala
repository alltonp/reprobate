package app.agent

import app.model.Incident
import app.server.AllRunsStatusUpdate
import im.mange.jetboot.Css._
import im.mange.jetboot.Html._
import im.mange.jetboot._
import im.mange.jetboot.bootstrap3.Bootstrap._
import im.mange.jetboot.bootstrap3.GridSystem
import im.mange.jetboot.bootstrap3.GridSystem._
import im.mange.jetboot.widget._

import scala.xml.Unparsed

//TODO: mouse over to see incident details ... start/finish etc
//TODO: should put incidents in tracking.txt
case class IncidentsAgent() extends Renderable {
  private val body = div(id = "incidentsBody").styles(fontSize(smaller))
  private val panel = div(body).styles(marginTop("15px"))

  def render = panel.render

  def onAllRunsStatusUpdate(update: AllRunsStatusUpdate) =
    if (update.totalIncidents == 0) body.empty else body.fill(layout(update))

  private def layout(update: AllRunsStatusUpdate) = GridSystem.row(col(12, render(update.openIncidents, update.closedIncidents)))

  //TODO: pull out presentation
  private def render(open: List[Incident], closed: List[Incident]) = {
    Composite(
      if (!open.isEmpty) tablify(headers("Open Incidents: " + open.size), rows(open)) else R(),
      if (!closed.isEmpty) tablify(headers("Closed Incidents: " + closed.size), rows(closed)) else R())
  }

  //TODO: ideally lets give 1 and 3 more room
  private def headers(prefix: String) = TableHeaders(List(
    TableHeader(span(prefix).styles(color("#0088cc"))).styles(width("27%")),
    TableHeader(R("Ref")).styles(width("7%")),
    TableHeader(R("Env")).styles(width("7%")),
    TableHeader(R("Reason")).styles(width("27%")),
    TableHeader(R("Opened")).styles(width("12%")),
    TableHeader(R("Closed")).styles(width("12%")),
    TableHeader(R("Duration")).styles(width("8%"))
  ))

  private def rows(incidents: List[Incident]) = incidents.map(i => TableRow(List(
    R(i.probe.description),
    R(s"IN-${i.id}"),
    R(i.probe.env),
    R(Unparsed(i.failures.mkString("<br/>"))),
    R(DateFormatForHumans.format(i.start)),
    R(i.finish.fold("-"){DateFormatForHumans.format(_)}),
    R(DateFormatForHumans.ago(i.openDuration))
  )))

  //TODO: pull out a widget
  private def tablify(h: TableHeaders, r: List[TableRow]) =
    div(SimpleTable(TableModel(h, r)).classes(tableCondensed, tableStriped).styles(marginBottom("0px"))).classes("round-corners")
}
