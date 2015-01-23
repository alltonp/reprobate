package jetboot.widget.bootstrap

import jetboot.Id._
import jetboot.{Renderable, Styles, Classes, Widget}
import jetboot.style.Bootstrap._

//TODO: I need thinking about a bit more, possibly I should be more like Badge than like Well
//TODO: make me Styleable
//TODO: use div() internally
case class Alert(id: String = ufe, classes: Classes = Classes(), styles: Styles = Styles(), content: Renderable) extends Widget {
  def render = <div id={id} class={classes.add(alert).render} style={styles.render}>{content.render}</div>
}