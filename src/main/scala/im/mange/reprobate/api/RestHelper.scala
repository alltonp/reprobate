package im.mange.reprobate.api

import net.liftweb.common.Box
import net.liftweb.http.{GetRequest, Req, LiftResponse}

trait RestHelper extends net.liftweb.http.rest.RestHelper {
  def run(probe: ⇒ Probe): () ⇒ Box[LiftResponse] = {
    () ⇒ Runner.run(probe)
  }

  object GET {
    def unapplySeq(in: Req): Option[Seq[String]] = in match {
      case Req(out, _, GetRequest) ⇒ Some(out)
      case _ ⇒ None
    }
  }
}
