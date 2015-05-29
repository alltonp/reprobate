package app.restlike.common

case class RefProvider(initial: Long) {
  private var count = initial

  def next = synchronized {
    count += 1
    s"$count"
  }
}
