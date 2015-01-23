package jetboot.widget.html

import jetboot._
import xml.NodeSeq

//TODO: I should have minimum show/hide, fill/empty
//TODO: I dont think I should be a case class actually - maybe be trait or abstract class with an Object
//TODO: if we stopped this being a Widget and Hideable, then id would no longer need to be mandatory
//TODO: show be fillable like a div?
case class Span(id: String, content: Renderable) extends Widget with Hideable with Styleable {
  def render = <span id={id} class={classes.render} style={styles.render}>{content.render}</span>
}

//TODO: er ... arent these Elements actually ... and Widgets are more highlevel - yes I should be in jetboot.html.element or something
