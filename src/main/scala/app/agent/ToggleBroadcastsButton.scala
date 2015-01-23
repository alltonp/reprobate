package app.agent

import jetboot.widget.ServerSideButton
import jetboot.widget.SimpleWidgets._
import jetboot.ButtonPresentation

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