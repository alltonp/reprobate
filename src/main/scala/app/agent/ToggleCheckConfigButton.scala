package app.agent

import im.mange.jetboot._
import im.mange.jetpac._
import im.mange.jetboot.widget.{Button, ButtonPresentation}

//TODO: theres a widget in here somewhere ... jetpac
case class ToggleCheckConfigButton(parent: RootAgent) extends ServerSideButton {
  val title = "Toggle Check Summary"

  private var shown = false

  def id = "toggleCheckConfig"

  //TODO: this blows somewhat
//  def presentation = ButtonPresentation(span().classes("glyphicon glyphicon-cog").render)
  def presentation = ButtonPresentation(span(R(<i class="fa fa-info" aria-hidden="true"></i>)).title(title).render)

  def onClick = {
    shown = !shown
    if (shown) parent.requestConfig else parent.hideConfig
  }
}

@deprecated("Use A() instead", "01/05/2015")
trait ServerSideButton extends Button {
  val title: String
  import net.liftweb.http.SHtml._
  //TODO: we do this in a few places .. id, class, style etc
  def render = a(() ⇒ onClick, presentation.body, "id" → id, "title" → title,
    "style" → presentation.styles.render,
    "class" → presentation.classes.render)
}
