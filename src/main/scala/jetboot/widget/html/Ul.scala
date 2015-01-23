package jetboot.widget.html

import jetboot._

case class Ul(content: Renderable) extends Renderable with Styleable {
  def render = <ul class={classes.render} style={styles.render}>{content.render}</ul>
}
