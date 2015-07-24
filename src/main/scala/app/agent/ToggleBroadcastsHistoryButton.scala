package app.agent

import im.mange.jetboot.widget.ButtonPresentation
import im.mange.jetboot.Html._

case class ToggleBroadcastsHistoryButton(parent: RootAgent) extends ServerSideButton {
  private var shown = false

  def id = "toggleBroadcastsHistory"

  def presentation = ButtonPresentation(span().classes("glyphicon glyphicon-bullhorn").render)

  def onClick = {
    shown = !shown
    if (shown) parent.showBroadcasts else parent.hideBroadcasts
  }
}