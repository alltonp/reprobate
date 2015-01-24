package app.agent

import app.model.Broadcast
import im.mange.jetboot.bootstrap3.Bootstrap
import im.mange.jetboot.widget._
import im.mange.jetboot.{Css, Html, R, Renderable}
import Css._
import Html._
import Bootstrap._
import scala.xml.Unparsed

//TODO: theres  a widget in here too ... jetpac
//TODO: consider making this live and encode the status in the row colour for active etc
case class BroadcastsPresentation(broadcasts: List[Broadcast]) extends Renderable {
  //TODO: improve the no broadcasts message formatting, this is horrible ... a table with just a header would be nicer
  def render = if (broadcasts.isEmpty) R("There are currently no broadcasts").render else renderTable

  private def renderTable = {
    val h = TableHeaders(List(
      TableHeader(span("Broadcasts: " + broadcasts.size).styles(color("#0088cc"))).styles(width("25%")),
      TableHeader(R("Message")) //.styles(width("10%"))
    ))

    val r = rows

    tablify(h, r).styles(fontSize(smaller)).render
  }

  private def rows = broadcasts.map(b => TableRow(List(
    R(DateFormatForHumans.format(b.when)),
    R(Unparsed(b.messages.mkString("<br/>")))
  )))

  //TODO: pull out a widget
  private def tablify(h: TableHeaders, r: List[TableRow]) =
    div(SimpleTable(TableModel(h, r)).classes(tableCondensed, tableStriped).styles(marginBottom("0px"))).classes("round-corners")
}
