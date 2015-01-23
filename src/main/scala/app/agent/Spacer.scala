package app.agent

import jetboot.Renderable

//TODO: I should be in jetboot
case class Spacer() extends Renderable {
  def render = <b>&nbsp;</b>
}
