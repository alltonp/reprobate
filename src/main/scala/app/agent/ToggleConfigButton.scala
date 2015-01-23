package app.agent

import jetboot.widget.ServerSideButton
import jetboot.widget.SimpleWidgets._
import jetboot.ButtonPresentation

//TODO: theres a widget in here somewhere ... jetpac
case class ToggleConfigButton(parent: RootAgent) extends ServerSideButton {
  private var shown = false

  def id = "toggleConfig"
  //TODO: this blows somewhat
  def presentation = ButtonPresentation(span().classes("glyphicon glyphicon-cog").render)

  def onClick = {
    shown = !shown
    if (shown) parent.requestConfig else parent.hideConfig
  }
}