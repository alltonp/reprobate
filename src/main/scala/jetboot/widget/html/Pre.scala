package jetboot.widget.html

import jetboot._

//TODO: I dont think I should be a case class actually - maybe be trait or abstract class with an Object
case class Pre(content: Renderable) extends Renderable with Styleable {
  def render = <pre class={classes.render} style={styles.render}>{content.render}</pre>
}

