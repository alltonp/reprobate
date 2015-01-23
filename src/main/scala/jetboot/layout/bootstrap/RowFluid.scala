package jetboot.layout.bootstrap

import jetboot.{Styleable, Renderable}
import jetboot.style.Bootstrap._

//TODO: internally - should I be a div()?
case class RowFluid(spans: Seq[SpanX]) extends Renderable with Styleable {
  def render = <div class={classes.add(rowFluid).render} style={styles.render}>{spans.map(_.render)}</div>
}

