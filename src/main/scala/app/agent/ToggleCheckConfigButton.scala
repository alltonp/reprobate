package app.agent

import im.mange.jetboot.Html._
import im.mange.jetboot.widget.{Button, ButtonPresentation}

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

@deprecated("Use A() instead", "01/05/2015")
trait ServerSideButton extends Button {
  import net.liftweb.http.SHtml._
  //TODO: we do this in a few places .. id, class, style etc
  def render = a(() ⇒ onClick, presentation.body, "id" → id, "style" → presentation.styles.render, "class" → presentation.classes.render)
}
