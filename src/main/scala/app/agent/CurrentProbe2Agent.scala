package app.agent

import jetboot.{R, Renderable}
import jetboot.widget.SimpleWidgets._
import app.server.CurrentRunStatusUpdate
import net.liftweb.http.js.JsExp
import net.liftweb.http.js.JE.{JsRaw, JsArray, JsObj}
import net.liftweb.http.js.JsCmds._
import jetboot.jscmd.JsCmdFactory._

case class DataPoint(key: String, value: JsExp)

object AsJsObj {
  def apply(dataPoints: List[DataPoint]) = {
    def keyValue(dp: DataPoint): (String, JsExp) = dp.key → dp.value

//    JsArray(dataPoints.map(dps ⇒ { JsObj( dps.map(keyValue(_) ): _*) }))
    JsObj(dataPoints.map(keyValue(_)): _*)
  }
}

//TODO: hide the panel until we have some content to show
//TODO: this is not a good name - it's more like CurrentRun
case class CurrentProbe2Agent() extends Renderable {

  private val id = "currentProbe2"
  private val body = div(id = id + "Body", R(<canvas id ={id + "Canvas"}></canvas>))
  private val panel = div(body)

  def render = panel.render

  //TODO: jetboot this up ....
  //TODO: introduce ProgressBar
  //TODO: row this up

  //TODO: use bootstrap colours?
  def onCurrentRunStatusUpdate(update: CurrentRunStatusUpdate) = {
    val presentation = """
                         var ctx = document.getElementById('""" + id + "Canvas" + """').getContext('2d');
                         new Chart(ctx).Doughnut(""" + id + "_abstraction" + """,{animation: false, animateScale: true, animateRotate: false});
                         """.stripMargin

    val model = List(
      AsJsObj(List(DataPoint("value", update.successPercent), DataPoint("color", "#00cc00"))),
      AsJsObj(List(DataPoint("value", update.inactivePercent), DataPoint("color", "#cccc00"))),
      AsJsObj(List(DataPoint("value", update.failurePercent), DataPoint("color", "#cc0000"))),
      AsJsObj(List(DataPoint("value", update.pendingPercent), DataPoint("color", "#D4CCC5")))
    )

    val generated: JsArray = JsArray(model)

    val hardcoded = JsRaw(
      """
var currentProbe2_abstraction = [{"value": "10", "color": "#00cc00"}, {"value": "50", "color": "#cc0000"}, {"value": "40", "color": "#cccc00"}];
      """.stripMargin)

    val hardcoded2 = JsRaw(
      """
var currentProbe2_abstraction = [{"value": "30", color: '#F7464A'}, {value: 50, color: '#46BFBD'}];
      """.stripMargin)

    val genModel: JsCrVar = JsCrVar(id + "_abstraction", generated)
    val r = genModel & JsRaw(presentation)

//    println("### " + r)

    nothing
//    r

//    body.fill(Composite(
//      div(
//        span(span().classes("glyphicon glyphicon-play"), R(" " + update.description)).styles(fontSize(smaller), fontWeight(bold)).classes(pullLeft),
//        span("Success: " + update.success + ", Failure: " + update.failure + ", Inactive: " + update.inactive + ", Total: " + update.of).styles(fontSize(smaller), fontWeight(bold)).classes(pullRight)
//      ).styles(clear(both)),
//      div(id = "progress",
//        R(
//          <div class="progress-bar progress-bar-success" style={"width: " + update.successPercent + "%"}>
//            <span class="sr-only">{update.successPercent}% Success</span>
//          </div>
//          <div class="progress-bar progress-bar-warning" style={"width: " + update.inactivePercent + "%"}>
//            <span class="sr-only">{update.inactivePercent}% Inactive</span>
//          </div>
//          <div class="progress-bar progress-bar-danger" style={"width: " + update.failurePercent + "%"}>
//            <span class="sr-only">{update.failurePercent}% Failure</span>
//          </div>
//        )
//
//      ).classes("progress", "progress-striped", "active").styles(clear(both), marginBottom("0px"))
//    ))
  }
}
