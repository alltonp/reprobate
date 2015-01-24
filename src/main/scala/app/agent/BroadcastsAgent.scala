package app.agent

import app.server.BroadcastsResponse
import im.mange.jetboot.{Html, Css, Renderable}
import Css._
import Html._

case class BroadcastsAgent() extends Renderable {
  private val holder = div("broadcasts").classes("hidden").styles(marginTop("5px"))

  def render = holder.render
  def onShowRequest = holder.show & holder.fill(BigSpinner("broadcastsSpinner", "Loading broadcasts..."))
  def onShowResponse(response: BroadcastsResponse) = holder.fill(BroadcastsPresentation(response.broadcasts))
  def onHide = holder.empty & holder.hide
}