package app.agent

import app.model.ProbeHistory
import im.mange.jetboot.bootstrap3.Bootstrap
import im.mange.jetboot.{R, Renderable}
import im.mange.jetboot.widget._
import im.mange.jetboot.Css._
import im.mange.jetboot.Html._
import Bootstrap._

//TODO: consider making this live and encode the status in the row colour for inactive, open etc
//TODO: rename to Probe checks or something
//TODO: maybe ENV first
//TODO: have a message when no checks configured
case class ProbeConfigPresentation(probes: List[ProbeHistory]) extends Renderable {
  def render = {
    val h = TableHeaders(List(
      TableHeader(span("Checks: " + probes.size).styles(color("#0088cc"))).styles(width("28%")),
      TableHeader(R("Environment")).styles(width("9%")),
      TableHeader(R("Active Period")).styles(width("9%")),
      TableHeader(R("Defcon")).styles(width("9%")),
      TableHeader(R("Executed")).styles(width("9%")),
      TableHeader(R("Failed")).styles(width("9%")),
      TableHeader(R("Inactive")).styles(width("9%")),
      TableHeader(R("Incidents")).styles(width("9%")),
      TableHeader(R("Raw")).styles(width("9%"))
    ))

    val r = rows ::: List(totalRow)

    tablify(h, r).styles(fontSize(smaller)).render
  }

  private def totalRow = TableRow(List(
    R("Total"),
    R("-"),
    R("-"),
    R("-"),
    R(probes.map(_.executedCount).sum.toString),
    R(probes.map(_.failedCount).sum.toString),
    R(probes.map(_.inactiveCount).sum.toString),
    R(probes.map(_.incidentCount).sum.toString),
    R("-")
  ))

  private def rows = probes.map(p => TableRow(List(
    R(p.probe.description),
    R(p.probe.env),
    R(p.probe.activePeriod.startHour + "-" + p.probe.activePeriod.finishHour),
    R(p.probe.defcon.level),
    R(p.executedCount.toString),
    R(p.failedCount.toString),
    R(p.inactiveCount.toString),
    R(p.incidentCount.toString),
    openInBrowser(p.probe.url)
  )))

  //TODO: defo can do less formatting here ...
  private def openInBrowser(probeUrl: String) = new LinkButton {
    val url = probeUrl

    override def presentation = ButtonPresentation(
      div(
        span(
          span().classes(glyphicon("link"))
        ).classes(pullLeft)//,
      ).styles(clear(both)).render
    )

    override def id: String = "openInBrowser"
  }

  //TODO: pull out a widget
  private def tablify(h: TableHeaders, r: List[TableRow]) =
    div(SimpleTable(TableModel(h, r)).classes(tableCondensed, tableStriped).styles(marginBottom("0px"))).classes("round-corners")
}
