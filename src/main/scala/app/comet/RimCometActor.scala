package app.comet

import app.ServiceFactory.rimServerActor
import app.agent.{CommandEditorAgent}
import app.restlike.common.Colours._
import app.restlike.rim.{Controller, Persistence, Presentation}
import app.server.ModelChanged
import im.mange.jetboot._
import im.mange.jetboot.widget.button.ToggleButton
import im.mange.jetpac._
import im.mange.jetpac.comet._
import im.mange.jetpac.css.{Classes, Styles}
import im.mange.jetpac.html.LinkAnchor
import im.mange.jetpac.page.StaticPage
import net.liftweb.common.Loggable
import net.liftweb.http.S
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.sitemap.Loc

import scala.reflect.ClassTag

abstract class CometPageWithRequestArgs[T: ClassTag] extends StaticPage {
  def cometActorName: String = implicitly[ClassTag[T]].runtimeClass.getSimpleName

  def render() = {
    println(S.request.get.param("foo"))
    println(S.request.get.params)
    //TODO: stash request params in a session var
    decorate( <lift:comet type={cometActorName}/> <div style="clear:both"></div> )
  }
}

//TODO: pull up
case class RimPage(override val path: String, override val params: Loc.LocParam[Any]*) extends CometPageWithRequestArgs[RimCometActor]

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

object RimToken {
  def token = Controller.tokensHead
  //TODO: these don't seem to work well locally on mac ... e.g. http://pauls-macbook.local:8473/rim/install/
  def script = s"http://${java.net.InetAddress.getLocalHost.getHostName}:8473/rim/install/$token"
  def state = s"http://${java.net.InetAddress.getLocalHost.getHostName}:8473/rim/state/$token"
  def tracking = s"http://${java.net.InetAddress.getLocalHost.getHostName}:8473/rim/tracking/$token"
}

//TODO: pull up
case class RimAgent(subscriber: im.mange.jetpac.comet.Subscriber) extends Renderable {
  import Html._

  println("refresh")
  //TODO: ultimately lookup "token" param from session - but use a read only token ...

  //TODO: this should be done automatically since it makes little sense here .. use email perhaps?
  //TODO: whatever, capture with original email

  private val backlogTerminal = Terminal("backlog"/*, Styles(fontSize(xSmall))*/)
  private val boardTerminal = Terminal("board", Styles(marginBottom("0px")))
  private val backlogToggle = ToggleButton("backlog", "Backlog", Classes("btn-xs btn-primary"), false, () => backlogTerminal.hide, () => backlogTerminal.show)
  private val boardToggle = ToggleButton("board", "Board", Classes("btn-xs btn-primary"), true, () => boardTerminal.hide, () => boardTerminal.show)

  private val commandEditorAgent = CommandEditorAgent(subscriber)

  def render = {
    import Css._

    div(
      Bs.containerFluid(
        Bs.row(col(12,
          div(
            span(boardToggle).styles(marginLeft("1px"), marginRight("1px")),
            span(backlogToggle).styles(marginLeft("2px")),
            span(
              LinkAnchor("", RimToken.script, span(R(<i class="fa fa-terminal" aria-hidden="true"></i>)).styles(marginRight("10px")), Some("_blank")),
              LinkAnchor("", RimToken.state, span(R(<i class="fa fa-database" aria-hidden="true"></i>)).styles(marginRight("10px")), Some("_blank")),
              LinkAnchor("", RimToken.tracking, span(R(<i class="fa fa-history" aria-hidden="true"></i>)).styles(marginRight("10px")), Some("_blank"))
            ).classes(pullRight)
          ).styles(/*textAlign(center), */marginBottom("0px")))
        ),
        Bs.row(col(12,
          div(
            div(
              div(boardTerminal).styles(display("table-cell")),
              div(backlogTerminal).styles(display("table-cell"))
            ).styles(display("table-row"), padding("1px"))
          ).styles(display("table"), width("100%"))
        )),
        Bs.row(col(12, commandEditorAgent))
      )
    ).render
  }

  def onInit = boardTerminal.init & boardTerminal.show & backlogTerminal.init & backlogTerminal.hide & commandEditorAgent.onInit

  def onModelChanged(changed: ModelChanged) = present(changed)

  private def present(modelChanged: ModelChanged): JsCmd = {
    //
    //TODO: we need this back again ....
    //
    //TODO: this needs to be a request arg ...
//    if (modelChanged.token != "4d30e06a-5107-4330-a8c7-7e9b472f716b" && modelChanged.token != "test") return Js.nothing
    if (modelChanged.token != RimToken.token && modelChanged.token != "test") return Js.nothing

    modelChanged.updated.fold(Js.nothing) { model =>
      val aka: Option[String] = None
      val changed = modelChanged.changedRefs

      //this is v. useful - http://labs.funkhausdesign.com/examples/terminal/cmd_controlled_terminal.html
      val what = List(customBlue("blue"), customGreen("green")).mkString("")
      val blessedTags = model.config.priorityTags

      val board = Presentation.board(model, changed, aka.getOrElse(""), hideBy = true)
      val phmv = Presentation.pointyHairedManagerView(model.issues.filter(_.status.isEmpty), blessedTags, model, aka.getOrElse(""), hideStatus = true, hideBy = true, hideTags = false, hideId = false, hideCount = false)

      boardTerminal.refresh(board.mkString("\n")) & backlogTerminal.refresh(phmv.mkString("\n"))
    }
  }
}

class RimCometActor extends RefreshableCometActor with MessageCapturingCometActor with Subscriber with Loggable {
  override def onCapturedMessage(message: Any) {}

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
    this ! ModelChanged(Persistence.load.modelForCli(RimToken.token), RimToken.token, Nil)
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