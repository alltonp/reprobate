package im.mange.reprobate.api

trait Probe {
  def probe: ProbeResponse
}

object AlwaysPassProbe extends Probe {
  import ProbeResponse._

  def probe = win
}
