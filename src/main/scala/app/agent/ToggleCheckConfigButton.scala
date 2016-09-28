package app.agent

import im.mange.jetboot.widget.ButtonPresentation
import im.mange.jetpac._

//TODO: theres a widget in here somewhere ... jetpac
case class ToggleCheckConfigButton(parent: RootAgent) extends ServerSideButton {
  val title = "Toggle Check Config"

  private var shown = false

  def id = "toggleCheckConfig"

  //TODO: this blows somewhat
//  def presentation = ButtonPresentation(span().classes("glyphicon glyphicon-cog").render)
  def presentation = ButtonPresentation(span(R(<i class="fa fa-cog" aria-hidden="true"></i>)).title(title).render)

  def onClick = {
    shown = !shown
    if (shown) parent.requestCheckSummary else parent.hideCheckSummary
  }
}


