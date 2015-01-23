package im.mange.reprobate.api

case class ProbeResponse(failures: List[String])

object ProbeResponse {
  def epicFail(failures: List[String]) = wrap(failures)
  def win = wrap(Nil)
  def wrap(failures: List[String]) = ProbeResponse(failures)
}

