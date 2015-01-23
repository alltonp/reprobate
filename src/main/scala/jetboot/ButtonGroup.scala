package jetboot

case class ButtonGroup(buttons: Button*) extends Renderable {
  //TODO: should be a fold, to guard against empty list
  def render = <div class="btn-group">{buttons.map(_.render).reduce(_ ++ _)}</div>
}
