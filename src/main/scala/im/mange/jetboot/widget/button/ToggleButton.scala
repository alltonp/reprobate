package im.mange.jetboot.widget.button

import im.mange.jetpac._
import im.mange.jetpac.css.Classes
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmd

case class ToggleButton(id: String, label: String, buttonClasses: Classes, expandedByDefault: Boolean = false, onCollapse: () => JsCmd, onExpand: () => JsCmd) extends Renderable {
  private var expanded = expandedByDefault
  private val link = span(Some(s"${id}_link"), icon())

  def render = {
    //TODO: use Html.a()

    R(SHtml.a(() => toggle(),
      <button type="button" class={s"btn ${buttonClasses.render}" + (if (expanded) " active" else "")} data-toggle="button" style="font-weight: bold;" id={id + "_toggle"}>{link.render}</button>,
      "style" -> "text-decoration: none;"
    )).render
  }

  private def toggle() = {
    if (expanded) {
      expanded = false
      collapse() & onCollapse()
    } else {
      expanded = true
      expand() & onExpand()
    }
  }

  private def expand() = link.fill(closeIcon())
  private def collapse() = link.fill(openIcon())
  private def openIcon() = R(<span><i class="fa fa-toggle-off fa-lg"></i>&nbsp;{label}</span>)
  private def closeIcon() = R(<span><i class="fa fa-toggle-on fa-lg"></i>&nbsp;{label}</span>)
  private def icon() = if (expanded) closeIcon() else openIcon()
}
