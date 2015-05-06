package app.agent

import app.server.BroadcastsResponse
import app.ui.BigSpinner
import im.mange.jetboot.{Html, Css, Renderable}
import Css._
import Html._

//TODO: is there a widget here ... something with a body div that we fill, empty etc ContainerAgent perhaps?
case class BroadcastsHistoryAgent() extends Renderable {
  private val holder = div(Some("broadcastsHistory")).classes("hidden").styles(marginTop("5px"))

  def render = holder.render
  def onShowRequest = holder.show & holder.fill(BigSpinner("broadcastsHistorySpinner", "Loading broadcasts..."))
  def onShowResponse(response: BroadcastsResponse) = holder.fill(BroadcastsPresentation(response.broadcasts))
  def onHide = holder.empty & holder.hide
}