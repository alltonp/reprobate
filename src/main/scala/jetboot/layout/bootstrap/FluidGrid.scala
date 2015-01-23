package jetboot.layout.bootstrap

import jetboot.{Composite, Renderable}

object FluidGrid {
  def container(rows: RowFluid*) = ContainerFluid(rows)
  def row(spans: SpanX*) = RowFluid(spans)
  def span_*(columns: Int, content: Renderable, offset: Int) = SpanX(columns, content, offset)
  def span_*(columns: Int, content: Renderable*): SpanX = span_*(columns, Composite(content:_*), 0)
  def span_*(columns: Int, content: List[Renderable]): SpanX = span_*(columns, Composite(content:_*), 0)
}

//TODO: think about a BootStrapLayoutBuilder or FluidGridBuilder etc