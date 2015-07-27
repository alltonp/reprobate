package app.comet

import app.ServiceFactory.{systemClock, rimServerActor}
import app.restlike.common.Colours
import app.restlike.rim.{Presentation, Persistence}
import app.server.ModelChanged
import im.mange.jetboot.comet._
import im.mange.jetboot.page.CometPage
import im.mange.jetboot.{Html, Js, R, Renderable}
import net.liftweb.actor.LiftActor
import net.liftweb.common.Loggable
import net.liftweb.http.S
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.sitemap.Loc
import im.mange.jetboot.Js
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

case class RimAgent(subscriber: im.mange.jetboot.comet.Subscriber) extends Renderable {
  import Html._

  println("refresh")
  private val params: Map[String, List[String]] = S.request.get.params
  //TODO: ultimately lookup "token" param - but use a read only token ...
  println(s"params: ${params}")

  private val holder = div(Some("term_demo"))

  def render = holder.render

  def onInit = createTerminal("term_demo")
  def onModelChanged(changed: ModelChanged) = present(changed)

  private def createTerminal(id: String) = {
    println("### init")
    val what = "hello"
    //TODO: not sure we need the function .. it doesnt need to echo anything right now
    val js = JsRaw("var terminal = $('#" + id + "').terminal(function(command, term) { term.echo('" + what + "'); }, {\n\t\t\t        name: '" + id + "',\n\t\t\t        prompt: '', \n\t\t\t        history: false,\n\t\t\t        enabled: false,\n\t\t\t        onFocus: function() { return false; }\n\t\t\t    } );")
    val js2 = JsRaw("terminal.clear();")
//    println(js)
    js & js2
  }

  private def present(modelChanged: ModelChanged): JsCmd = {
//    if (modelChanged.token != "4d30e06a-5107-4330-a8c7-7e9b472f716b") return Js.nothing

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
      //    val js3 = JsRaw("terminal.echo('" + s"update ${systemClock().dateTime} - $modelChanged - $params" + "');")
      //    val js3 = JsRaw("terminal.echo('" + Presentation.board(model, changed, aka.getOrElse("")) + "');")
      val matching = model.issues
      val blessedTags = List.empty[String]
      val sanitise = false

      //    val whatToShow = Unparsed(what)
      //    val whatToShow = Unparsed(pointyHairedManagerView.mkString("[lb]").replaceAll("\n", "<br />"))
      def echo(l: String): JsCmd = {
        JsRaw( s"""terminal.echo("$l");""")
      }

      val board = Presentation.board(model, changed, aka.getOrElse(""), hideBy = true)
      val backlog = Presentation.backlog(model, aka)
      val phmv = Presentation.pointyHairedManagerView("release", matching, blessedTags, model, aka.getOrElse(""), hideStatus = true, hideBy = true, hideTags = false, hideId = false, hideCount = false)
      val whatToShow = board.mkString("\n") + "\n" + backlog.mkString("\n") + "\n" + phmv.mkString("\n")

      //.replaceAll("\n", "<br />")
      val js3 = whatToShow.split("\n").map(l => echo(l)).toSeq
      //    val js3 = JsRaw(s"""terminal.echo("$whatToShow");""")
      //    println(whatToShow)
      //    println(js3)

      js2 & Js.chain(js3)
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