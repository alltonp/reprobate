package jetboot.layout.bootstrap

import jetboot.Renderable
import jetboot.style.Bootstrap._

case class ContainerFluid(rowFluids: Seq[RowFluid]) extends Renderable {
  def render = <div class={containerFluid}>{rowFluids.map(_.render)}</div>
}
