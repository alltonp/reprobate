package jetboot.widget.bootstrap

import xml.NodeSeq
import jetboot._
import jetboot.Classes
import jetboot.Styles
import Id._
import jetboot.style.Bootstrap._

//TODO: I should have minimum show/hide, fill/empty
//TODO: I share common stuff with span and div, should factor out
//TODO: can some of these renders be val?
//TODO: I should take a Renderable as well
//TODO: I wonder if classes and styles could be a var arg ....
//TODO: make me Styleable
//TODO: I need to take a Renderable not a NodeSeq
//TODO: use div() internally
case class Well(id: String = ufe, classes: Classes = Classes(), styles: Styles = Styles(), content: NodeSeq = NodeSeq.Empty) extends Widget with Hideable {
  def render = <div id={id} class={classes.add(well).render} style={styles.render}>{content}</div>
//
//  def this(id: String = ufe, classes: Classes = Classes(), styles: Styles = Styles(), content: Renderable) =
//    this(id, classes, styles, content.render)
}

//TODO: make a BootstrapWidgets for this stuff