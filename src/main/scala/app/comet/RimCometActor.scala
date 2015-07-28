package app.comet

import app.ServiceFactory.{systemClock, rimServerActor}
import app.restlike.common.Colours
import app.restlike.rim.{Presentation, Persistence}
import app.server.ModelChanged
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
  private val holder = div(Some(id))
  private val instance = s"${id}_terminal"

  def render = holder.render

  def init = {
    println("### init")
    val what = "hello"
    //TODO: not sure we need the function .. it doesnt need to echo anything right now
    //TODO: need to include id in the var name and stash it
    val js = JsRaw("var " + instance + " = $('#" + id + "').terminal(function(command, term) { term.echo('" + what + "'); }, {\n\t\t\t        name: '" + instance + "',\n\t\t\t        prompt: '', \n\t\t\t        history: false,\n\t\t\t        enabled: false,\n\t\t\t        onFocus: function() { return false; }\n\t\t\t    } );")
    val js2 = clear
    js & js2
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
        row(col(6, backlogTerminal), col(6, boardTerminal))
      )
    ).render
  }

  def onInit = backlogTerminal.init & boardTerminal.init

  def onModelChanged(changed: ModelChanged) = present(changed)

  private def present(modelChanged: ModelChanged): JsCmd = {
    //TODO: we need this back again ....
    if (modelChanged.token != "4d30e06a-5107-4330-a8c7-7e9b472f716b") return Js.nothing

    modelChanged.updated.fold(Js.nothing) { model =>
      //    println("### present")

//      R(s"update ${systemClock().dateTime} - $modelChanged - $params")

//      R(modelChanged.updated.issues.map(i => div(None, R(i.description))))
//      TODO: this is essentially groupByStatus in disguise - we should share it ..
//      modelChanged.updated.issues.groupBy(_.status)

      val includeBacklog = true
      val includeReleased = false
      val compressEmptyStates = false
      val hideBy = true
      val hideTags = false
      val aka: Option[String] = None
      val changed = Seq()
      val currentModel = modelChanged.updated
//      val model = modelChanged.updated

//      val stateToIssues = model.issues.groupBy(_.status.getOrElse("backlog"))
//      val interestingStates = (if (includeBacklog) List("backlog") else Nil) ::: currentModel.workflowStates ::: (if (includeReleased) List("released") else Nil)
//      val r = interestingStates.map(s => {
//        val issuesForState = stateToIssues.getOrElse(s, Nil)
//        //TODO: this is the pure view bit
//        val issues = issuesForState.map(i => s"\n  ${
//          i.render(model, hideStatus = true, hideBy = hideBy, hideTags = hideTags, highlight = changed.contains(i.ref), highlightAka = aka)
//        }").mkString
//        if (issuesForState.isEmpty && compressEmptyStates) None else Some(s"$s: (${issuesForState.size})" + issues + "\n")
//        //end view bit
//      }).flatten
//
//      holder.fill(R(r))

      val id = "term_demo"
//      val value = r
      //    JsRaw("$('#" + id + "').echo('" + value + "');")

      //    val js = JsRaw("$('#" + id + "').terminal(function(command, term) { term.echo('you just typed test'); }, { prompt: '>', name: 'test' } ););")

      //this is v. useful - http://labs.funkhausdesign.com/examples/terminal/cmd_controlled_terminal.html
      val what = List(Colours.customBlue("blue"), Colours.customGreen("green")).mkString("")
//      val newWhat: String = r.head.replaceAll("\\(", "").replaceAll("\\)", "")
      val js2 = JsRaw("terminal.clear();")
      val blessedTags = model.priorityTags

      val board = Presentation.board(model, changed, aka.getOrElse(""), hideBy = true)
//      val backlog = Presentation.backlog(model, aka)
      val phmv = Presentation.pointyHairedManagerView("release", model.issues.filter(_.status.isEmpty), blessedTags, model, aka.getOrElse(""), hideStatus = true, hideBy = true, hideTags = false, hideId = false, hideCount = false)
      val whatToShow = board.mkString("\n") + "\n" /*+ backlog.mkString("\n")*/ + "\n" + phmv.mkString("\n")

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

//    R(<div id="term_demo" class="terminal" style="height: 200px;"></div>)
//    R(s"update ${systemClock().dateTime} - $modelChanged - $params")
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