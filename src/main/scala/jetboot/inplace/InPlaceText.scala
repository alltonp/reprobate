package jetboot.inplace

import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE.JsRaw

//TODO: make disabled until init
//TODO: set placeholder = default
//TODO: optionally support clear
case class InPlaceText(id: String, default: String, options: Options, placeholder: String = "") extends InPlaceEditor {
  //TODO: pull up
  enrich(options)

  def render = <span><a href="#" id={id} data-clear="false" data-type="text" data-placeholder={placeholder} class="editable editable-click">{default}</a> &nbsp;</span>
  def init = JsRaw("$('#" + id + "').editable(" + options.toJsCmd + ");")
  def currentValue = element.getText

  private def enrich(options: Options) { options.inputclasses = editorClass :: options.inputclasses }
}
