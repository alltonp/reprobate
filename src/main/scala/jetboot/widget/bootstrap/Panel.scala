package jetboot.widget.bootstrap

import jetboot.{Styleable, R, Renderable}
import jetboot3.Bootstrap

//TODO: should be bootstrap3
//TODO: maybe should id's be Option generally?
case class Panel(id: String, body: Renderable, heading: Option[Renderable] = None, footer: Option[Renderable] = None) extends Renderable with Styleable {
 import jetboot.widget.SimpleWidgets._
 import Bootstrap._

  def render =
    div(id = id,
      heading.fold(R()){h => div(id = id + "_heading", h).classes(panelHeading)},
      div(id = id + "_body", body).classes(panelBody),
      footer.fold(R()){f => div(id = id + "_footer", f).classes(panelFooter)}
    )
    .classes(classes.add(panel, panelDefault))
    .styles(styles)
    .render
}
