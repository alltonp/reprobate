package jetboot.widget.html

import jetboot._
import xml.NodeSeq

case class Li(content: Renderable) extends Renderable with Styleable {
  def render = <li class={classes.render} style={styles.render}>{content.render}</li>
}
