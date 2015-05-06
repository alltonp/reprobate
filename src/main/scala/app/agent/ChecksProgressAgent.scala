package app.agent

import app.server.CurrentRunStatusUpdate
import im.mange.jetboot.Css._
import im.mange.jetboot.Html._
import im.mange.jetboot._

case class ChecksProgressAgent() extends Renderable {
  private val body = div(id = Some("checksProgress"))
  private val panel = div(body).styles(marginBottom("10px"))

  def render = panel.render

  //TODO: jetboot this up ....
  //TODO: introduce ProgressBar into jetboot
  //TODO: row this up

  def onCurrentRunStatusUpdate(update: CurrentRunStatusUpdate) = body.fill(progressBar(update))

  private def progressBar(update: CurrentRunStatusUpdate) =
    div(id = Some("progress"), R(
      <div class="progress-bar progress-bar-success" style={"width: " + update.successPercent + "%"}>
          <span class="sr-only"> {update.successPercent}% Success</span>
        </div>
          <div class="progress-bar progress-bar-warning" style={"width: " + update.inactivePercent + "%"}>
            <span class="sr-only">{update.inactivePercent}% Inactive</span>
          </div>
          <div class="progress-bar progress-bar-danger" style={"width: " + update.failurePercent + "%"}>
            <span class="sr-only">{update.failurePercent}% Failure</span>
          </div>
      )
    ).classes("progress", "progress-striped", "active").styles(clear(both), marginBottom("0px"))
}
