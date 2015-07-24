package app.comet

//TODO: kill this and use the jetboot one
trait Subscriber {
  def !(msg: Any)
}