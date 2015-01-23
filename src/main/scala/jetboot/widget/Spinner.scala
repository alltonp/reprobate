package jetboot.widget

import jetboot.{Element, Styleable, Renderable}

//TODO: this should take a Renderable for the content instead
//TODO: this does actually look like an InlineSpinner as its a span (which looks like it should be a Fillable)
case class Spinner(id: String, message: String, imgSrc: String) extends Renderable with Styleable {
  private val element = Element(id)

  //TODO: looks like this should be a span()
  def render = <span id={id} class={classes.render} style={styles.render}><img src={imgSrc}/> <b> {message}</b></span>
  def start = element.show
  def stop = element.hide
}
