package app.agent

import app.server.BroadcastsResponse
import jetboot.Css._
import jetboot.Renderable
import jetboot.widget.SimpleWidgets._

case class BroadcastsAgent() extends Renderable {
  private val holder = div("broadcasts").classes("hidden").styles(marginTop("5px"))

  def render = holder.render
  def onShowRequest = holder.show & holder.fill(BigSpinner("broadcastsSpinner", "Loading broadcasts..."))
  def onShowResponse(response: BroadcastsResponse) = holder.fill(BroadcastsPresentation(response.broadcasts))
  def onHide = holder.empty & holder.hide
}