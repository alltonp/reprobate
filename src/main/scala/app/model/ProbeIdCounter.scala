package app.model

case class ProbeIdCounter() {
  private var count = 0

  def next = synchronized {
    count += 1
    "%05d".format(count)
  }
}
