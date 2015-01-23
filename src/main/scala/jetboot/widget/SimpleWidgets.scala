package jetboot.widget

import jetboot.widget.html._
import jetboot.Renderable
import jetboot.Id._
import jetboot.widget.html.Span
import jetboot.R
import jetboot.widget.html.Div
import jetboot.Composite
import jetboot.widget.html.Ul
import jetboot.widget.html.Li

//TODO: this should be SimpleElements or HtmlElements or maybe just Html (so we can import Html._)
object SimpleWidgets {
  //TODO: think about Div and AnonDiv (if no id)
  def div(id: String, content: Renderable*) = Div(id, Composite(content:_*))
  def div(content: Renderable*): Div = div(ufe, content:_*)

  def li(content: Renderable*) = Li(Composite(content:_*))

  //TODO: think about Span and AnonSpan (if no id)
  def span(id: String, content: Renderable*) = Span(id, Composite(content:_*))
  def span(content: Renderable*): Span = span(ufe, Composite(content:_*))
  def span(content: String): Span = span(R(content))

  def ul(content: Renderable*) = Ul(Composite(content:_*))

  def table(thead: Thead, tbody: Tbody) = Table(thead, tbody)
  def thead(content: Renderable*) = Thead(Composite(content:_*))
  def tbody(content: Renderable*) = Tbody(Composite(content:_*))
  def tr(content: Renderable*) = Tr(Composite(content:_*))
  def th(content: Renderable) = Th(content)
  def th(content: String): Th = th(R(content))
  def td(content: Renderable) = Td(content)
  def td(content: String): Td = td(R(content))

  def pre(content: String) = Pre(R(content))

  //TODO: actually widgets should always be Identifiable ... otherwise they are a Renderable
}
