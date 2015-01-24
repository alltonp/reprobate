package app.agent

import app.server.Message
import im.mange.jetboot.Css._
import im.mange.jetboot.Html._
import im.mange.jetboot.widget.Spacer
import im.mange.jetboot.{R, Renderable}

//TODO: seems to be a container-tastic
case class StatusMessageAgent() extends Renderable {
  private val body = div(id = "messageBody").styles(float(left), paddingBottom("10px"))//.styles(display(inlineBlock))
  private val panel = span(body)//.classes().styles(marginBottom("10px"))

  def render = panel.render

  def onMessage(message: Message) = body.fill(
    span(R(<small>{message.subject}</small>), Spacer(), R(message.detail)).classes("h3")
  )
}
