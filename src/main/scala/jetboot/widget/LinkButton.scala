package jetboot.widget

import jetboot.Button

trait LinkButton extends Button {
  import jetboot.jscmd.JsCmdFactory._

  //TODO: target should be configurable
  def url: String
  val onClick = nothing
  def render = <a href={url} id={id} target="_blank" class={presentation.classes.render} style={presentation.styles.render}>{presentation.body}</a>
}
