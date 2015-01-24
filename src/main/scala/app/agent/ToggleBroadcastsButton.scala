package app.agent

import im.mange.jetboot.widget.{ButtonPresentation, ServerSideButton}
import im.mange.jetboot.Html._

//TODO: theres a widget in here somewhere ... jetpac
case class ToggleBroadcastsButton(parent: RootAgent) extends ServerSideButton {
  private var shown = false

  def id = "toggleBroadcasts"
  //TODO: this blows somewhat
  def presentation = ButtonPresentation(span().classes("glyphicon glyphicon-bullhorn").render)

  def onClick = {
    shown = !shown
    if (shown) parent.showBroadcasts else parent.hideBroadcasts
  }
}