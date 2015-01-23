package jetboot.inplace

import jetboot.jscmd.JsCmdFactory._

case class InPlaceHidden(id: String, value: String) extends InPlaceEditor {
  def render = <input type="hidden" id={id} value={value}/>
  def init = nothing
  def currentValue = element.getValue
}
