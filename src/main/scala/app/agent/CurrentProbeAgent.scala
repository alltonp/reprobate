package app.agent

import app.server.CurrentRunStatusUpdate
import im.mange.jetboot.Css._
import im.mange.jetboot.Html._
import im.mange.jetboot._

//TODO: this is not a good name - it's more like CurrentProbeStatus
case class CurrentProbeAgent() extends Renderable {

  private val body = div(id = "currentProbeBody")
  private val panel = div(body).styles(marginBottom("10px"))

  def render = panel.render

  //TODO: jetboot this up ....
  //TODO: introduce ProgressBar into jetboot
  //TODO: row this up

  def onCurrentRunStatusUpdate(update: CurrentRunStatusUpdate) = body.fill(progressBar(update))

  private def progressBar(update: CurrentRunStatusUpdate) =
    div(id = "progress", R(
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

//  private def description(update: CurrentRunStatusUpdate)=
//    div(
//      span(span().classes("glyphicon glyphicon-play"), R(" " + DateFormatForHumans.timeNow + " " + update.description)).styles(fontSize(smaller), fontWeight(bold)).classes(pullLeft),
//      span("Success: " + update.success + ", Failure: " + update.failure + ", Inactive: " + update.inactive + ", Total: " + update.of).styles(fontSize(smaller), fontWeight(bold)).classes(pullRight)
//    ).styles(clear(both))
}
