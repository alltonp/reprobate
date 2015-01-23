package jetboot.widget.bootstrap

import jetboot.{Renderable, Id, Widget}
import xml.{Elem, NodeSeq, Text}
import Id._

//TODO: widgets for LabelWithSpace etc
//TODO: should consider using Classes
//TODO: use the bootstrap style enum thing
//TODO: (maybe, not completely convinced yet) make me Styleable
//TODO: should be updatable like a Badge
//TODO: use span() internally
case class Label(value: String, status: String = "") extends Renderable {
  def render = <span class={"label " + status}>{value}</span>
}
