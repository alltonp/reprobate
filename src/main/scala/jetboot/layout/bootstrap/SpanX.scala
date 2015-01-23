package jetboot.layout.bootstrap

import jetboot.{Styleable, Renderable}
import xml.NodeSeq

//TODO: internally - should I be a div?
case class SpanX(columns: Int, content: Renderable, offset: Int = 0) extends Renderable with Styleable {
  private val spanClasses = "span" + columns + (if (offset > 0) " offset" + offset else "")
  def render = <div class={classes.add(spanClasses).render} style={styles.render}>{content.render}</div>
}
