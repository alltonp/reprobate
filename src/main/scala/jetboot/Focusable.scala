package jetboot

trait Focusable extends Identifiable {
  def focus = element.focus
}
