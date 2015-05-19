package app.agent

import app.model.ChecksHistory
import im.mange.jetboot.Css._
import im.mange.jetboot.Html._
import im.mange.jetboot.bootstrap3.Bootstrap._
import im.mange.jetboot.widget.SimpleTable._
import im.mange.jetboot.widget._
import im.mange.jetboot.widget.table.{TableHeaders, TableRow}
import im.mange.jetboot.{R, Renderable}

//TODO: consider making this live and encode the status in the row colour for inactive, open etc
//TODO: maybe ENV first
//TODO: have a message when no checks configured
case class ChecksConfigPresentation(checks: List[ChecksHistory]) extends Renderable {
  def render = {
    val h = headers(List(
      header(span(None, "Checks: " + checks.size).styles(color("#0088cc"))).styles(width("25%")),
      header(R("Environment")).styles(width("9%")),
      header(R("Raw")).styles(width("6%")),
      header(R("Active Period")).styles(width("10%")),
      header(R("Defcon")).styles(width("10%")),
      header(R("Executed")).styles(width("10%")),
      header(R("Failed")).styles(width("10%")),
      header(R("Inactive")).styles(width("10%")),
      header(R("Incidents")).styles(width("10%"))
    ))

    val r = rows ::: List(totalRow)

    tablify(h, r).styles(fontSize(smaller)).render
  }

  private def totalRow = TableRow(None, List(
    R("Total"),
    R("-"),
    R("-"),
    R("-"),
    R("-"),
    R(checks.map(_.executedCount).sum.toString),
    R(checks.map(_.failedCount).sum.toString),
    R(checks.map(_.inactiveCount).sum.toString),
    R(checks.map(_.incidentCount).sum.toString)
  ))

  private def rows = checks.map(p => TableRow(None, List(
    R(p.probe.description),
    R(p.probe.env),
    openInBrowser(p.probe.url),
    R(p.probe.activePeriod.startHour + "-" + p.probe.activePeriod.finishHour),
    R(p.probe.defcon.level),
    R(p.executedCount.toString),
    R(p.failedCount.toString),
    R(p.inactiveCount.toString),
    R(p.incidentCount.toString)
  )))

  //TODO: defo can do less formatting here ...
  private def openInBrowser(probeUrl: String) = new LinkButton {
    val url = probeUrl

    override def presentation = ButtonPresentation(
      div(
        span(
          span().classes(glyphicon("link"))
        ).classes(pullLeft)
      ).styles(clear(both)).render
    )

    override def id: String = "openInBrowser"
  }

  //TODO: pull out a widget
  private def tablify(h: TableHeaders, r: List[TableRow]) =
    div(None, simpleTable(h, r).classes(tableCondensed, tableStriped).styles(marginBottom("0px"))).classes("round-corners")
}
