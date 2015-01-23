package app.view

import xml.{Elem, NodeSeq}

object AppView {
  def page(content: NodeSeq): Elem = <div class="lift:surround?with=app;at=content">{ content }</div>
  def apply() = page(<div><lift:comet type="AppCometActor"/><div style="clear:both"/></div>)
}