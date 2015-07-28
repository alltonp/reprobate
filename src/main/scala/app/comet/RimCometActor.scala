package app.comet

import app.ServiceFactory.{systemClock, rimServerActor}
import app.restlike.common.Colours
import app.restlike.common.Colours._
import app.restlike.rim.{Presentation, Persistence}
import app.server.ModelChanged
import im.mange.jetboot.Css._
import im.mange.jetboot.Html._
import im.mange.jetboot.bootstrap3.Bootstrap
import im.mange.jetboot.bootstrap3.GridSystem._
import im.mange.jetboot.comet._
import im.mange.jetboot.css.{Styles, Classes}
import im.mange.jetboot.page.CometPage
import im.mange.jetboot._
import net.liftweb.actor.LiftActor
import net.liftweb.common.Loggable
import net.liftweb.http.{SHtml, S}
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.sitemap.Loc
import net.liftweb.http.js.JE.{JsRaw, ValById}
import net.liftweb.http.js.JsCmds.{SetHtml, _}
import net.liftweb.http.js.jquery.JqJE.{JqAttr, JqGetAttr, JqId, JqPrepend, JqRemove, JqReplace, _}
import net.liftweb.http.js.{JsCmd, JsExp, JsMember}

import net.liftweb.http.js.JE.{JsRaw, ValById}
import net.liftweb.http.js.JsCmds.{SetHtml, _}
import net.liftweb.http.js.jquery.JqJE.{JqAttr, JqGetAttr, JqId, JqPrepend, JqRemove, JqReplace, _}
import net.liftweb.http.js.{JsCmd, JsExp, JsMember}

import scala.xml.Unparsed

//TODO: pull up
case class RimPage(override val path: String, override val params: Loc.LocParam[Any]*) extends CometPage[RimCometActor]

//TODO: pull up
case class Terminal(id: String, styles: Styles = Styles()) extends Renderable with Hideable {
  private val holder = div(Some(id)).classes("round-corners").styles(styles)
  private val instance = s"${id}_terminal"

  def render = holder.render

  def init = {
    val what = ""
    //TODO: not sure we need the function .. it doesnt need to echo anything right now
    val js = JsRaw("var " + instance + " = $('#" + id + "').terminal(function(command, term) { term.echo('" + what + "'); }, {\n\t\t\t        name: '" + instance + "',\n\t\t\t        prompt: '', \n\t\t\t        history: false,\n\t\t\t        enabled: false,\n\t\t\t        onFocus: function() { return false; }\n\t\t\t    } );")
    js & clear
  }

  def refresh(what: String) = clear & Js.chain(what.split("\n").map(l => echo(l)).toSeq)

  override def hide = holder.hide
  override def show = holder.show

  private def clear = JsRaw(s"${instance}.clear();")
  private def echo(line: String): JsCmd = JsRaw( s"""${instance}.echo("$line");""")
}

//TODO: move to jetboot
case class ToggleButton(id: String, label: String, buttonClasses: Classes, expandedByDefault: Boolean = false, onCollapse: () => JsCmd, onExpand: () => JsCmd) extends Renderable {
  private var expanded = expandedByDefault
  private val link = span(Some(s"${id}_link"), icon())

  def render = {
    //TODO: use Html.a()

    R(SHtml.a(() => toggle(),
      <button type="button" class={s"btn ${buttonClasses.render}" + (if (expanded) " active" else "")} data-toggle="button" style="font-weight: bold;" id={id + "_toggle"}>{link.render}</button>,
      "style" -> "text-decoration: none;"
    )).render
  }

  private def toggle() = {
    if (expanded) {
      expanded = false
      collapse() & onCollapse()
    } else {
      expanded = true
      expand() & onExpand()
    }
  }

  private def expand() = link.fill(closeIcon())
  private def collapse() = link.fill(openIcon())
  private def openIcon() = R(<span><i class="fa fa-toggle-off fa-lg"></i>&nbsp;{label}</span>)
  private def closeIcon() = R(<span><i class="fa fa-toggle-on fa-lg"></i>&nbsp;{label}</span>)
  private def icon() = if (expanded) closeIcon() else openIcon()
}

//TODO: pull up
case class RimAgent(subscriber: im.mange.jetboot.comet.Subscriber) extends Renderable {
  import Html._

  println("refresh")
  private val params: Map[String, List[String]] = S.request.get.params
  //TODO: ultimately lookup "token" param - but use a read only token ...
  println(s"params: ${params}")

  private val backlogTerminal = Terminal("backlog"/*, Styles(fontSize(xSmall))*/)
  private val boardTerminal = Terminal("board"/*, Styles(fontSize(xSmall))*/)
  private val backlogToggle = ToggleButton("backlog", "Backlog", Classes("btn-xs btn-primary"), false, () => backlogTerminal.hide, () => backlogTerminal.show)
  private val boardToggle = ToggleButton("board", "Board", Classes("btn-xs btn-primary"), true, () => boardTerminal.hide, () => boardTerminal.show)

  def render = {
    import im.mange.jetboot.bootstrap3.GridSystem._
    import Css._

    div(
      containerFluid(
        row(col(12, div(
            span(boardToggle).styles(marginLeft("1px"), marginRight("2px")),
            span(backlogToggle).styles(marginLeft("2px"))
          ).styles(/*textAlign(center), */marginBottom("0px")))
        ),
        row(col(12, div(
//          div(R()).styles(width("1px"), display("table-cell"), padding("0px")),
          div(
            div(boardTerminal).styles(display("table-cell")),
            div(backlogTerminal).styles(display("table-cell"))
          ).styles(display("table-row"), padding("0px"))
        ).styles(display("table"), width("100%"))
      ))
    )).render
  }

  def onInit = boardTerminal.init & boardTerminal.show & backlogTerminal.init & backlogTerminal.hide

  def onModelChanged(changed: ModelChanged) = present(changed)

  private def present(modelChanged: ModelChanged): JsCmd = {
    //
    //TODO: we need this back again ....
    //
    if (modelChanged.token != "4d30e06a-5107-4330-a8c7-7e9b472f716b" && modelChanged.token != "test") return Js.nothing
    //
    //
    //

    modelChanged.updated.fold(Js.nothing) { model =>
      val aka: Option[String] = None
      val changed = modelChanged.changedRefs

      //this is v. useful - http://labs.funkhausdesign.com/examples/terminal/cmd_controlled_terminal.html
      val what = List(customBlue("blue"), customGreen("green")).mkString("")
      val blessedTags = model.priorityTags

      val board = Presentation.board(model, changed, aka.getOrElse(""), hideBy = true)
      val phmv = Presentation.pointyHairedManagerView("release", model.issues.filter(_.status.isEmpty), blessedTags, model, aka.getOrElse(""), hideStatus = true, hideBy = true, hideTags = false, hideId = false, hideCount = false)

      boardTerminal.refresh(board.mkString("\n")) & backlogTerminal.refresh(phmv.mkString("\n"))
    }

//    $('#some_id').terminal(function(command, term) {
//      if (command == 'test') {
//        term.echo("you just typed 'test'");
//      } else {
//        term.echo('unknown command');
//      }
//    }, { prompt: '>', name: 'test' });

//    $('#some_id').terminal(function(command, term) {
//        term.echo("you just typed 'test'");
//    }, { prompt: '>', name: 'test' });
  }
}

class RimCometActor extends RefreshableCometActor with MessageCapturingCometActor with Subscriber with Loggable {
  override def onCapturedMessage(message: Any, actor: LiftActor) {}

  private var rootAgent: RimAgent = _

  def beforeRefresh() {
//    println("beforeRefresh")
    //root.cleanup()
    rimServerActor() ! Unsubscribe(this)
  }

  def doRefresh() {
//    println("doRefresh")
    rootAgent = new RimAgent(this)
  }

  def afterRefresh(): Unit = {
//    println("afterRefresh")
    rimServerActor() ! Subscribe(this)
    //TODO: this forces refresh
    this ! app.server.Init()
    //TODO: pull out obv
    this ! ModelChanged(Persistence.load.modelFor("4d30e06a-5107-4330-a8c7-7e9b472f716b"), "4d30e06a-5107-4330-a8c7-7e9b472f716b", Nil)
  }

  def doRender = {
//    println("doRender")
    rootAgent.render
  }

  override def localShutdown() {
//    println("localShutdown")
    //root.cleanup()
    rimServerActor() ! Unsubscribe(this)
    super.localShutdown()
  }

  override def onMessage = handleMessage :: super.onMessage

  //TODO: pull out commands for all these
  private def handleMessage: PartialFunction[Any, Unit] = {
    case m:app.server.Init => /*println("got: " + m);*/ partialUpdate(rootAgent.onInit)
    case m:ModelChanged => /*println(m); */ partialUpdate(rootAgent.onModelChanged(m))
    case e => logger.error(s"${getClass.getSimpleName}: unexpected message received: $e")
  }
}