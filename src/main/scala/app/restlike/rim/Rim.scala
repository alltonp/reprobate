package app.restlike.rim

//import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, StandardOpenOption}

import app.restlike.rim.Responder._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.json._

import scala.collection.immutable
import scala.reflect.io.File

object Messages {
  val eh = "- eh?"

  def notAuthorised(who: String) = List(s"- easy ${who}, please set your initials first ⇒ 'rim aka pa'")
  def notFound(ref: String) = problem("issue not found: $ref")
  def problem(message: String) = List(s"problem - $message")

  //TODO: how about advance and retreat instead of forward/back or push/pull or left/right
  def help(who: String) = List(
    s"hello ${who}, welcome to rim - rudimental issue management © 2015 spabloshi ltd",
    "",
    "issues:",
    "  - create              ⇒ 'rim + [the description]'",
    "  - update              ⇒ 'rim [ref] ='",
    "  - delete              ⇒ 'rim [ref] -'",
    "  - list/search         ⇒ 'rim ? {query}'",
    "  - own                 ⇒ 'rim [ref] @'",
//    "  - tag                 ⇒ 'rim [ref] ^ [tag]'",
//    "  - detag               ⇒ 'rim [ref] ^- [tag]'",
    "",
    "board:",
    "  - show                ⇒ 'rim'",
    "  - add/move forward    ⇒ 'rim [ref] /'",
    "  - move to end         ⇒ 'rim [ref] //'",
    "  - move backward       ⇒ 'rim [ref] .'",
    "  - return to backlog   ⇒ 'rim [ref] ..'",
    "",
    "releases:",
    "  - create              ⇒ 'rim release [tag]'",
    "  - list                ⇒ 'rim releases'",
    "",
    "other:",
    "  - set aka             ⇒ 'rim aka [initials]'",
//    "  - tags                ⇒ 'rim tags'",
    "  - get help            ⇒ 'rim help'",
//    "",
    "experts:",
    "  - create and forward  ⇒ 'rim +/ description'",
    "  - create and end      ⇒ 'rim +// description'",
    ""
  )

  val install =
    (s"""#!/bin/bash
      |#INSTALLATION:
      |#- alias rim='{path to}/rim.sh'
      |#- that's it!
      |
      |RIM_HOST="http://${java.net.InetAddress.getLocalHost.getHostName}:8473"
      |""" + """OPTIONS="--timeout=15 --no-proxy -qO-"
      |WHO=`id -u -n`
      |BASE="rim/$WHO"
      |REQUEST="$OPTIONS $RIM_HOST/$BASE"
      |MESSAGE="${@:1}"
      |RESPONSE=`wget $REQUEST --post-data="{\"value\":\"${MESSAGE}\"}" --header=Content-Type:application/json`
      |printf "\n$RESPONSE\n\n"
      |
    """).stripMargin.split("\n").toList
}

case class IssueRef(initial: Long) {
  private var count = initial

  def next = synchronized {
    count += 1
    s"$count"
  }
}

case class Issue(ref: String, description: String, status: Option[String], by: Option[String]) {
  private val indexed = List(ref, description, status.getOrElse("")).mkString(" ")

  def search(query: String) = indexed.contains(query)
  //TODO: ${state.fold("")(":" + _)}
  def render = s"$ref: $description ${by.fold("")("@" + _.toUpperCase)}"
}

case class Release(tag: String, issues: List[Issue])

case class Model(workflowStates: List[String], userToAka: immutable.Map[String, String], issues: List[Issue], released: List[Release]) {
  def createIssue(args: List[String], status: Option[String] = None, by: Option[String] = None) = {
    val newRef = Controller.issueRef.next
    val description = args.mkString(" ")
    val created = Issue(newRef, description, status, by)
    val updatedModel = this.copy(issues = created :: this.issues)
    (created, updatedModel)
  }

  def aka(who: String) = userToAka(who)
  def findIssue(ref: String) = issues.find(_.ref == ref)
  def beginState = workflowStates.head
  def endState = workflowStates.reverse.head
  def releasableIssues = issues.filter(_.status == Some(endState))
  def releaseTags = released.map(_.tag)
}

case class In(head: Option[String], tail:List[String])
case class Out(messages: List[String] = Nil, updatedModel: Option[Model] = None)

object Commander {
  def process(cmd: In, who: String, currentModel: Model): Out = {
    if (!cmd.head.getOrElse("").equals("aka") && !Controller.knows_?(who)) return Out(Messages.notAuthorised(who), None)

    //TODO: be nice of the help could be driven off this ...
    cmd match {
      case In(Some(""), Nil) => onShowBoard(currentModel)
      case In(Some("aka"), List(aka)) => onAka(who, aka, currentModel)
      case In(Some("help"), Nil) => onHelp(who, currentModel)
      case In(Some("+"), args) => onAddIssue(args, currentModel)
      case In(Some("+/"), args) => onAddAndBeginIssue(who, args, currentModel)
      case In(Some("+//"), args) => onAddAndEndIssue(who, args, currentModel)
      case In(Some("?"), Nil) => onQueryIssues(currentModel, None)
      case In(Some("?"), List(query)) => onQueryIssues(currentModel, Some(query))
      case In(Some(ref), List("-")) => onRemoveIssue(ref, currentModel)
      case In(Some(ref), args) if args.nonEmpty && args.head == "=" => onEditIssue(ref, args.drop(1), currentModel)
      case In(Some(ref), List("/")) => onForwardIssue(who, ref, currentModel)
      case In(Some(ref), List("//")) => onFastForwardIssue(who, ref, currentModel)
      case In(Some(ref), List(".")) => onBackwardIssue(who, ref, currentModel)
      case In(Some(ref), List("..")) => onFastBackwardIssue(who, ref, currentModel)
      case In(Some(ref), List("@")) => onOwnIssue(who, ref, currentModel)
      case In(Some("release"), List(tag)) => onRelease(tag, currentModel)
      case In(Some("releases"), Nil) => onShowReleases(currentModel)
      case In(head, tail) => onUnknownCommand(head, tail)
    }
  }

  private def onUnknownCommand(head: Option[String], tail: List[String]) =
    Out(Messages.eh + " " + head.getOrElse("") + " " + tail.mkString(" ") :: Nil, None)

  private def onShowBoard(currentModel: Model) = Out(Presentation.board(currentModel), None)

  private def onHelp(who: String, currentModel: Model) = Out(Messages.help(currentModel.aka(who)), None)

  private def onShowReleases(currentModel: Model) = {
    val all = currentModel.released.map(Presentation.release(_)).flatten
    Out(all, None)
  }

  private def onRelease(tag: String, currentModel: Model): Out = {
    val releaseable = currentModel.releasableIssues
    val remainder = currentModel.issues diff releaseable

    if (currentModel.releaseTags.contains(tag)) return Out(Messages.problem(s"$tag has already been released"), None)
    if (releaseable.isEmpty) return Out(Messages.problem(s"nothing to release for $tag"), None)

    val release = Release(tag, releaseable)
    val updatedModel = currentModel.copy(issues = remainder, released = release :: currentModel.released )

    Out(Presentation.release(release), Some(updatedModel))
  }

  private def onOwnIssue(who: String, ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val updated = found.copy(by = Some(currentModel.userToAka(who)))
      val index = currentModel.issues.indexOf(found)
      val updatedModel = currentModel.copy(issues = currentModel.issues.updated(index, updated))
      Out(s"@ ${found.render}" :: Nil, Some(updatedModel))
    }
  }

  private def onBackwardIssue(who: String, ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newStatus = if (found.status.isEmpty) None
      else {
        val currentIndex = currentModel.workflowStates.indexOf(found.status.get)
        if (currentIndex <= 0) None else Some(currentModel.workflowStates(currentIndex - 1))
      }
      val updated = found.copy(status = newStatus, by = Some(currentModel.userToAka(who)))
      val index = currentModel.issues.indexOf(found)
      val updatedModel = currentModel.copy(issues = currentModel.issues.updated(index, updated))
      Out(Presentation.board(updatedModel), Some(updatedModel))
    }
  }

  private def onFastBackwardIssue(who: String, ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newStatus = None
      val updated = found.copy(status = newStatus, by = None)
      val index = currentModel.issues.indexOf(found)
      val updatedModel = currentModel.copy(issues = currentModel.issues.updated(index, updated))
      Out(Presentation.board(updatedModel), Some(updatedModel))
    }
  }

  private def onForwardIssue(who: String, ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newStatus = if (found.status.isEmpty) currentModel.beginState
      else {
        val currentIndex = currentModel.workflowStates.indexOf(found.status.get)
        val newIndex = if (currentIndex >= currentModel.workflowStates.size - 1) currentIndex else currentIndex + 1
        currentModel.workflowStates(newIndex)
      }
      val updated = found.copy(status = Some(newStatus), by = Some(currentModel.userToAka(who)))
      val index = currentModel.issues.indexOf(found)
      val updatedModel = currentModel.copy(issues = currentModel.issues.updated(index, updated))
      Out(Presentation.board(updatedModel), Some(updatedModel))
    }
  }

  private def onFastForwardIssue(who: String, ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newStatus = currentModel.endState
      val updated = found.copy(status = Some(newStatus), by = Some(currentModel.userToAka(who)))
      val index = currentModel.issues.indexOf(found)
      val updatedModel = currentModel.copy(issues = currentModel.issues.updated(index, updated))
      Out(Presentation.board(updatedModel), Some(updatedModel))
    }
  }

  private def onEditIssue(ref: String, args: List[String], currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newDescription = args.mkString(" ")
      val updated = found.copy(description = newDescription)
      val index = currentModel.issues.indexOf(found)
      val updatedModel = currentModel.copy(issues = currentModel.issues.updated(index, updated))
      Out(s"= ${updated.render}" :: Nil, Some(updatedModel))
    }
  }

  private def onRemoveIssue(ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val updatedModel = currentModel.copy(issues = currentModel.issues.filterNot(i => i == found))
      Out(s"- ${found.render}" :: Nil, Some(updatedModel))
    }
  }

  //TODO: add search to Model
  private def onQueryIssues(currentModel: Model, query: Option[String]) = {
    val matching = query.fold(currentModel.issues)(q => currentModel.issues.filter(i => i.search(q)))
    val result = if (matching.isEmpty) (s"no issues found" + (if (query.isDefined) s" for: ${query.get}" else "")) :: Nil
    else matching.reverse.map(i => i.render)
    Out(result, None)
  }

  private def onAddIssue(args: List[String], currentModel: Model) = {
    val (created, updatedModel) = currentModel.createIssue(args)
    Out(s"+ ${created.render}" :: Nil, Some(updatedModel))
  }

  private def onAddAndBeginIssue(who: String, args: List[String], currentModel: Model) = {
    val (_, updatedModel) = currentModel.createIssue(args, Some(currentModel.beginState), Some(currentModel.aka(who)))
    Out(Presentation.board(updatedModel), Some(updatedModel))
  }

  private def onAddAndEndIssue(who: String, args: List[String], currentModel: Model) = {
    val (_, updatedModel) = currentModel.createIssue(args, Some(currentModel.endState), Some(currentModel.aka(who)))
    Out(Presentation.board(updatedModel), Some(updatedModel))
  }

  private def onAka(who: String, aka: String, currentModel: Model): Out = {
    if (aka.size > 3) return Out(Messages.problem("maximum 3 chars"), None)
    val updatedModel = currentModel.copy(userToAka = currentModel.userToAka.updated(who, aka.toUpperCase))
    Out(Messages.help(aka.toUpperCase), Some(updatedModel))
  }
}

object Presentation {
  def board(model: Model) = {
    val stateToIssues = model.issues.groupBy(_.status)
    model.workflowStates.map(s => {
      val issuesForState = stateToIssues.getOrElse(Some(s), Nil)
      val issues = issuesForState.map(i => s"\n  ${i.render}").mkString
      s"$s: (${issuesForState.size})" + issues + "\n"
    })
  }
  
  def release(release: Release) = s"${release.tag}:" :: release.issues.map(i => s"  ${i.render}")
}

object Controller {
  private var model = Persistence.load
  val issueRef = IssueRef(if (model.issues.isEmpty) 0 else model.issues.map(_.ref).max.toLong)

  def process(who: String, req: Req): Box[LiftResponse] =
    JsonRequestHandler.handle(req)((json, req) ⇒ {
      val value = RimRequestJson.deserialise(pretty(render(json))).value.toLowerCase.trim
      println(s"=> $who: [${value}]")
      val bits = value.split(" ").map(_.trim)

      synchronized {
        val out = Commander.process(In(bits.headOption, bits.tail.toList), who, model)
        out.updatedModel.map(m => {
          model = m
          Persistence.save(model)
        })
        t(out.messages)
      }

      //TODO:
      //check for dupes when adding ...
      //log all commands somewhere
      //show count of issues
      //show count of releases
      //store the when? (arguably it could be in the log)
    })

  //TODO: this should exclude me ...
  def knows_?(who: String) = model.userToAka.contains(who)
}

object Persistence {
  private val file = Paths.get("rim.json")
  private val defaultStatuses = List("next", "doing", "done")

  def load: Model = {
    if (!file.toFile.exists()) save(Model(defaultStatuses, immutable.Map[String, String](), List[Issue](), List[Release]()))
    Json.deserialise(Filepath.load(file))
  }

  def save(state: Model) {
    Filepath.save(pretty(render(Json.serialise(state))), file)
  }
}

//TODO: use the one in little instead
object Filepath {
  def save(content: String, path: Path) = {
    Files.write(path, content.getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
  }

  def append(content: String, path: Path) = {
    Files.write(path, content.getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)
  }

  def load(path: Path) = File(path.toFile).slurp()

  def create(path: Path) = Files.createFile(path)
}


//TODO: handle corrupted rim.json

//TODO: protect against empty value
//TODO: discover common keys and present them when updating
//TODO: be careful with aka .. they need to be unique
//TODO: on update, don't show self in list of others and don't show anything if others are empty
//TODO: make it possible to ask questions and force others to answer them
//TODO: colourise
//http://stackoverflow.com/questions/287871/print-in-terminal-with-colors-using-python?rq=1
//http://apple.stackexchange.com/questions/74777/echo-color-coding-stopped-working-in-mountain-lion
//http://unix.stackexchange.com/questions/43408/printing-colored-text-using-echo
//e.g. printf '%s \e[0;31m%s\e[0m %s\n' 'Some text' 'in color' 'no more color'
//  def red(value: String) = s"\e[1;31m $value \e[0m"

//TODO: (maybe) support curl
//#MESSAGE="(Foo) Deployed ${VERSION} to ${MACHINE_NAME}"
//#curl --connect-timeout 15 -H "Content-Type: application/json" -d "{\"message\":\"${MESSAGE}\"}" http://localhost:8765/broadcast
//#wget --timeout=15 --no-proxy -O- --post-data="{\"message\":\"${MESSAGE}\"}" --header=Content-Type:application/json "http://localhost:8765/broadcast"

