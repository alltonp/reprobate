package app.agent

import im.mange.jetboot.Html._
import im.mange.jetboot.widget.{ButtonPresentation, ServerSideButton}

//TODO: theres a widget in here somewhere ... jetpac
case class ToggleCheckConfigButton(parent: RootAgent) extends ServerSideButton {
  private var shown = false

  def id = "toggleCheckConfig"

  //TODO: this blows somewhat
  def presentation = ButtonPresentation(span().classes("glyphicon glyphicon-cog").render)

  def onClick = {
    shown = !shown
    if (shown) parent.requestConfig else parent.hideConfig
  }
}