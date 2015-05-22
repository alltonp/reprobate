package app.agent

import app.server.Message
import im.mange.jetboot.Css._
import im.mange.jetboot.Html._
import im.mange.jetboot.widget.Spacer
import im.mange.jetboot.{R, Renderable}

//TODO: is there a widget here ... something with a body div that we fill, empty etc ContainerAgent perhaps?
case class StatusMessageAgent() extends Renderable {
  private val body = div(id = Some("statusMessageBody")).styles(float(left), paddingBottom("10px"))
  private val panel = span(body)

  def render = panel.render

  def onMessage(message: Message) = body.fill(
    span(R(<small>{message.subject}</small>), Spacer(), R(message.detail)).classes("h4")
  )
}
