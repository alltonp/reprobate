package app.comet

import app.ServiceFactory.{systemClock, rimServerActor}
import app.restlike.common.Colours
import app.restlike.common.Colours._
import app.restlike.rim.{Presentation, Persistence}
import app.server.ModelChanged
import im.mange.jetboot.Css._
import im.mange.jetboot.Html._
import im.mange.jetboot.bootstrap3.GridSystem._
import im.mange.jetboot.comet._
import im.mange.jetboot.page.CometPage
import im.mange.jetboot._
import net.liftweb.actor.LiftActor
import net.liftweb.common.Loggable
import net.liftweb.http.S
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

case class RimPage(override val path: String, override val params: Loc.LocParam[Any]*) extends CometPage[RimCometActor]

case class Terminal(id: String) extends Renderable {
  private val holder = div(Some(id)).styles(fontSize(xSmall))
  private val instance = s"${id}_terminal"

  def render = holder.render

  def init = {
    val what = ""
    //TODO: not sure we need the function .. it doesnt need to echo anything right now
    val js = JsRaw("var " + instance + " = $('#" + id + "').terminal(function(command, term) { term.echo('" + what + "'); }, {\n\t\t\t        name: '" + instance + "',\n\t\t\t        prompt: '', \n\t\t\t        history: false,\n\t\t\t        enabled: false,\n\t\t\t        onFocus: function() { return false; }\n\t\t\t    } );")
    js & clear
  }

  def show(what: String) = clear & Js.chain(what.split("\n").map(l => echo(l)).toSeq)

  private def clear = JsRaw(s"${instance}.clear();")
  private def echo(line: String): JsCmd = JsRaw( s"""${instance}.echo("$line");""")
}

case class RimAgent(subscriber: im.mange.jetboot.comet.Subscriber) extends Renderable {
  import Html._

  println("refresh")
  private val params: Map[String, List[String]] = S.request.get.params
  //TODO: ultimately lookup "token" param - but use a read only token ...
  println(s"params: ${params}")

  private val backlogTerminal = Terminal("backlog")
  private val boardTerminal = Terminal("board")

  def render = {
    import im.mange.jetboot.bootstrap3.GridSystem._
    import Css._

    div(
      containerFluid(
        row(col(6, div(R("Backlog")).styles(textAlign(center), marginBottom("7px"))), col(6, div(R("Board")).styles(textAlign(center), marginBottom("7px")))),
        row(col(6, div(backlogTerminal)), col(6, boardTerminal))
      )
    ).render
  }

  def onInit = backlogTerminal.init & boardTerminal.init

  def onModelChanged(changed: ModelChanged) = present(changed)

  private def present(modelChanged: ModelChanged): JsCmd = {
    //TODO: we need this back again ....
    if (modelChanged.token != "4d30e06a-5107-4330-a8c7-7e9b472f716b") return Js.nothing

    modelChanged.updated.fold(Js.nothing) { model =>
      val aka: Option[String] = None
      val changed = Seq()

      //this is v. useful - http://labs.funkhausdesign.com/examples/terminal/cmd_controlled_terminal.html
      val what = List(customBlue("blue"), customGreen("green")).mkString("")
      val blessedTags = model.priorityTags

      //TODO: support recently changed ...
      val board = Presentation.board(model, changed, aka.getOrElse(""), hideBy = true)
      val phmv = Presentation.pointyHairedManagerView("release", model.issues.filter(_.status.isEmpty), blessedTags, model, aka.getOrElse(""), hideStatus = true, hideBy = true, hideTags = false, hideId = false, hideCount = false)

      backlogTerminal.show(phmv.mkString("\n")) & boardTerminal.show(board.mkString("\n"))
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
    this ! ModelChanged(Persistence.load.modelFor("4d30e06a-5107-4330-a8c7-7e9b472f716b"), "4d30e06a-5107-4330-a8c7-7e9b472f716b")
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