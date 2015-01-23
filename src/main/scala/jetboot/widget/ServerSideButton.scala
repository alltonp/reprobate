package jetboot.widget

import jetboot.Button
import net.liftweb.http.SHtml._

//TODO: support "data-toggle" → "button"
trait ServerSideButton extends Button {
  //TODO: we do this in a few places .. id, class, style etc
  def render = a(() ⇒ onClick, presentation.body, "id" → id, "style" → presentation.styles.render, "class" → presentation.classes.render)
}