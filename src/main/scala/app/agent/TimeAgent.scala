package app.agent

import app.ServiceFactory.{dateFormats, systemClock}
import app.server.Message
import im.mange.jetboot.Css._
import im.mange.jetboot.Html._
import im.mange.jetboot.widget.Spacer
import im.mange.jetboot.{R, Renderable}

//TODO: is there a widget here ... something with a body div that we fill, empty etc ContainerAgent perhaps?
case class TimeAgent() extends Renderable {

  private val body = div(id = Some("timeBody")).styles(float(right), paddingBottom("10px"))
  private val panel = span(body)

  def render = panel.render

  def onMessage(message: Message) = body.fill(R(
    span(R(<small>{"now"}</small>), Spacer(), R(dateFormats().shortDateTimeFormat.print(systemClock().dateTime))).classes("h3")
  ))
}
