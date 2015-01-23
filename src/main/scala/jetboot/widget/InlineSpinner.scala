package jetboot.widget

import jetboot.{Element, Styleable, Renderable}

//TODO: this should take a Renderable for the content instead
//TODO: this looks like it should be a Fillable
case class InlineSpinner(id: String, message: String, imgSrc: String, initiallyHidden: Boolean = false) extends Renderable with Styleable {
  private val element = Element(id)

  //TODO: looks like this should be a span()
  def render = <span id={id} class={classes.add(if (initiallyHidden) "hidden" else "").render} style={styles.render}><img src={imgSrc}/> <b> {message}</b></span>
  def start = element.show
  def stop = element.hide
}
