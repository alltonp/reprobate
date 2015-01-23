package jetboot

import xml.NodeSeq

//TODO: looks like these are types of span() actually .. and should be Styleable etc
//TODO: if these are indeed bootstrap buttons they should probably have 'btn' by default
object ButtonPresentationFactory {
  def iconAndLabel(icon: Icon, label: String, classes: Classes = Classes(), styles: Styles = Styles(), padding: Boolean = false) = {
    val body = if (padding) <span>&nbsp;{icon.render}&nbsp;&nbsp;{label}&nbsp;</span> else <span>{icon.render}&nbsp;&nbsp;{label}</span>
    ButtonPresentation(body, classes, styles)
  }

  def iconOnly(icon: Icon, classes: Classes = Classes(), styles: Styles = Styles(), padding: Boolean = false) = {
    val body = if (padding) <span>&nbsp;{icon.render}&nbsp;</span> else <span>{icon.render}</span>
    ButtonPresentation(body, classes, styles)
  }

  def labelOnly(label: String, classes: Classes = Classes(), styles: Styles = Styles(), padding: Boolean = false) = {
    val body = if (padding) <span>&nbsp;{label}&nbsp;</span> else <span>{label}</span>
    ButtonPresentation(body, classes, styles)
  }
}

trait Icon {
  def render: NodeSeq
}

//TODO: move this stuff to a bootstrap package
case class Glyphicon(name: String, white: Boolean = false) extends Icon {
  override def render: NodeSeq = <i class={"icon-" + name + (if (white) " icon-white" else "") }/>
}

//TODO: move this stuff to a fontawesome package
//TODO: add in the other stuff
case class FontAwesomeIcon(name: String, size: String = "") extends Icon {
  override def render: NodeSeq = <i class={"icon-" + name + " icon-" + size}/>
}

object IconFactory {
  def glyphicon(name: String, white: Boolean = false) = Glyphicon(name, white)
  //should do glyphicon-pro

  //font awesome
  def default(name: String) = FontAwesomeIcon(name)
  def large(name: String) = FontAwesomeIcon(name, "large")
  def x2(name: String) = FontAwesomeIcon(name, "2x")
  def x3(name: String) = FontAwesomeIcon(name, "3x")
  def x4(name: String) = FontAwesomeIcon(name, "4x")
}

//TODO: possibly it's an icon builder actually (well named args)
