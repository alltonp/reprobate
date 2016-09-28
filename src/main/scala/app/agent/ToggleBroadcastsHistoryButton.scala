package app.agent

import im.mange.jetboot.widget.ButtonPresentation
import im.mange.jetboot._
import im.mange.jetpac._

case class ToggleBroadcastsHistoryButton(parent: RootAgent) extends ServerSideButton {
  val title = "Toggle Broadcast History"

  private var shown = false

  def id = "toggleBroadcastsHistory"

  def presentation = ButtonPresentation(span(R(<i class="fa fa-bullhorn" aria-hidden="true"></i>)).render)

  def onClick = {
    shown = !shown
    if (shown) parent.showBroadcasts else parent.hideBroadcasts
  }
}