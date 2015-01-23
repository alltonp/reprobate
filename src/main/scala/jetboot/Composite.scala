package jetboot

import xml.NodeSeq

//TODO: we do this a lot - consider C()
//TODO: allow creation from a Seq too .. otherwise we end up doing a lot of :_*
case class Composite(renderables: Renderable*) extends Renderable {
  def render = renderables.foldLeft(NodeSeq.Empty) {(result, r) ⇒ result ++ r.render }
}
