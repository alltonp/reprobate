package jetboot.widget.html

import jetboot._

//TODO: I should have minimum show/hide, fill/empty
//TODO: for fill() to work we need a proper div, need to think about that a bit
//TODO: I dont think I should be a case class actually - maybe be trait or abstract class with an Object
//TODO: er ... arent these Elements actually ... and Widgets are more highlevel - yes I should be in jetboot.html.element or something
//TODO: SimpleWidgets could mixin all the Bootstrap style, StyleKeys and StyleVals stuff for ease of typing/conversion (StyleFactory)
//TODO: maybe have a LayoutFactory to match
//TODO: possibly WidgetFactory is ElementFactory actually
case class Div(id: String, content: Renderable) extends Widget with Hideable with Styleable with HtmlElement {
  def render = <div id={id} class={classes.render} style={styles.render} title={title}>{content.render}</div>

  //TODO: these should be on fillable I think
  def fill(content: Renderable) = element.fill(content)
  def empty = element.empty
}