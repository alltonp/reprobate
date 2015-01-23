package jetboot.widget.bootstrap

import jetboot.{R, Id, Widget}
import Id._

//TODO: widgets for Label, LabelWithSpace etc
//TODO: should consider using Classes
//TODO: use the bootstrap style enum thing
//TODO: support updates to the count
//TODO: also make a BadgeAndLabel widget
//TODO: (maybe, not completely convinced yet) make me Styleable
//TODO: use span() internally
case class Badge(id: String = ufe, value: String, status: String = "") extends Widget {
  def render = <span id={id} class={"badge " + status}>{value}</span>
  def update(value: String) = element.fill(R(value))
}
