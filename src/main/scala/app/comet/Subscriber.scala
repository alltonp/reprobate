package app.comet

trait Subscriber {
  def !(msg: Any)
}