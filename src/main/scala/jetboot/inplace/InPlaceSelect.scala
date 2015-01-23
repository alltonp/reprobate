package jetboot.inplace

import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE.JsRaw

//TODO: make disabled until init
case class InPlaceSelect(id: String, default: String, options: Options) extends InPlaceEditor {
  //TODO: pull up
  enrich(options)

  def render = <span><a href="#" id={id} data-type="select" data-value={default} class="editable editable-click">{default}</a> &nbsp;</span>
  def init = JsRaw("$('#" + id + "').editable(" + options.toJsCmd + ");")
  def currentValue = element.getText

  private def enrich(options: Options) { options.inputclasses = editorClass :: options.inputclasses }
}
