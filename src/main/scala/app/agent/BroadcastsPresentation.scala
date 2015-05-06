package app.agent

import app.DateFormatForHumans
import app.model.Broadcast
import im.mange.jetboot.Css._
import im.mange.jetboot.Html._
import im.mange.jetboot.bootstrap3.Bootstrap._
import im.mange.jetboot.widget._
import im.mange.jetboot.widget.table.{TableHeader, TableHeaders, TableRow}
import im.mange.jetboot.{R, Renderable}

import scala.xml.Unparsed

//TODO: theres  a widget in here too ... jetpac
//TODO: consider making this live and encode the status in the row colour for active etc
case class BroadcastsPresentation(broadcasts: List[Broadcast]) extends Renderable {
  import SimpleTable._

  //TODO: improve the no broadcasts message formatting, this is horrible ... a table with just a header would be nicer
  def render = if (broadcasts.isEmpty) R("There are currently no broadcasts").render else renderTable

  private def renderTable = {
    val h = TableHeaders(List(
      TableHeader(span(None, "Broadcasts: " + broadcasts.size).styles(color("#0088cc"))).styles(width("25%")),
      TableHeader(R("Message")) //.styles(width("10%"))
    ))

    val r = rows

    tablify(h, r).styles(fontSize(smaller)).render
  }

  private def rows = broadcasts.map(b => TableRow(None, List(
    R(DateFormatForHumans.format(b.when)),
    R(Unparsed(b.messages.mkString("<br/>")))
  )))

  //TODO: pull out a widget
  private def tablify(h: TableHeaders, r: List[TableRow]) =
    div(None, simpleTable(h, r).classes(tableCondensed, tableStriped).styles(marginBottom("0px"))).classes("round-corners")
}
