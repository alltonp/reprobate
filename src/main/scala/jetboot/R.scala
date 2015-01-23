package jetboot

import xml.{Text, NodeSeq}

//TODO: do we need an S for Styleable?
object R {
  def apply(content: String): Renderable = R(Text(content))
  def apply(): Renderable = R(NodeSeq.Empty)
}

case class R(private val content: NodeSeq) extends Renderable {
  def render = content
}
