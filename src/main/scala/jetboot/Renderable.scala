package jetboot

import xml.NodeSeq

trait Renderable {
  def render: NodeSeq
}
