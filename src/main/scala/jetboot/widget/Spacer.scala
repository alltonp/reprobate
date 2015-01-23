package jetboot.widget

import jetboot.Renderable

case class Spacer() extends Renderable {
  def render = <b>&nbsp;</b>
}
