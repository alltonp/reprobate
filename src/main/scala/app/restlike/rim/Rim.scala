package app.restlike.rim

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths, StandardOpenOption}

import app.restlike.rim.Responder._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.json._

import scala.collection.immutable
import scala.io.Source

object Messages {
  val eh = "- eh?"

  def notAuthorised(who: String) = List(s"- easy ${who}, please set your initials first ⇒ 'rim aka pa'")
  def notFound(ref: String) = problem("issue not found: $ref")
  def problem(message: String) = List(s"problem - $message")

  def help(who: String) = List(
    s"hello ${who}, welcome to rim - rudimental issue management © 2015 spabloshi ltd",
    "",
    "issues:",
    "  - add ⇒ 'rim + [the description]'",
    "  - list ⇒ 'rim ? {query}'",
    "  - delete ⇒ 'rim [ref] -'",
    "  - edit ⇒ 'rim [ref] ='",
    "  - own ⇒ 'rim [ref] @'",
    "",
    "releases:",
    "  - show board ⇒ 'rim'",
    "  - add/move forward ⇒ 'rim [ref] /'",
    "  - move to end ⇒ 'rim [ref] //'",
    "  - move backward ⇒ 'rim [ref] .'",
    "  - return to backlog ⇒ 'rim [ref] ..'",
//    "  - release ⇒ 'rim release [tag]'",
    "",
    "other:",
    "  - set aka ⇒ 'rim aka [initials]'",
    "  - get help ⇒ 'rim help'"
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

case class Model(workflowStates: List[String], userToAka: immutable.Map[String, String], issues: List[Issue]) {
  def aka(who: String) = userToAka(who)
  def findIssue(ref: String) = issues.find(_.ref == ref)
}

case class In(head: Option[String], tail:List[String])
case class Out(messages: List[String], updatedState: Option[Model])

object Commander {
  def process(cmd: In, who: String, currentState: Model): Out = {
    if (!cmd.head.getOrElse("").equals("aka") && !Controller.knows_?(who)) return Out(Messages.notAuthorised(who), None)

    //TODO: be nice of the help could be driven off this ...
    cmd match {
      case In(Some(""), Nil) => onShowBoard(currentState)
      case In(Some("aka"), List(aka)) => onAka(who, aka, currentState)
      case In(Some("help"), Nil) => onHelp(who, currentState)
      case In(Some("+"), args) => onAddIssue(args, currentState)
      case In(Some("?"), Nil) => onQueryIssues(currentState, None)
      case In(Some("?"), List(query)) => onQueryIssues(currentState, Some(query))
      case In(Some(ref), List("-")) => onRemoveIssue(ref, currentState)
      case In(Some(ref), args) if args.nonEmpty && args.head == "=" => onEditIssue(ref, args.drop(1), currentState)
      case In(Some(ref), List("/")) => onForwardIssue(who, ref, currentState)
      case In(Some(ref), List("//")) => onFastForwardIssue(who, ref, currentState)
      case In(Some(ref), List(".")) => onBackwardIssue(who, ref, currentState)
      case In(Some(ref), List("..")) => onFastBackwardIssue(who, ref, currentState)
      case In(Some(ref), List("@")) => onOwnIssue(who, ref, currentState)
      case In(head, tail) => onUnknownCommand(head, tail)
    }
  }

  private def onUnknownCommand(head: Option[String], tail: List[String]) =
    Out(Messages.eh + " " + head.getOrElse("") + " " + tail.mkString(" ") :: Nil, None)

  private def onShowBoard(currentState: Model) = Out(Presentation.board(currentState), None)

  private def onHelp(who: String, currentState: Model) = Out(Messages.help(currentState.aka(who)), None)

  private def onOwnIssue(who: String, ref: String, currentState: Model) = {
    currentState.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val updated = found.copy(by = Some(currentState.userToAka(who)))
      val index = currentState.issues.indexOf(found)
      val updatedState = currentState.copy(issues = currentState.issues.updated(index, updated))
      Out(s"@ ${found.render}" :: Nil, Some(updatedState))
    }
  }

  private def onBackwardIssue(who: String, ref: String, currentState: Model) = {
    currentState.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newStatus = if (found.status.isEmpty) None
      else {
        val currentIndex = currentState.workflowStates.indexOf(found.status.get)
        if (currentIndex <= 0) None else Some(currentState.workflowStates(currentIndex - 1))
      }
      val updated = found.copy(status = newStatus, by = Some(currentState.userToAka(who)))
      val index = currentState.issues.indexOf(found)
      val updatedState = currentState.copy(issues = currentState.issues.updated(index, updated))
      Out(Presentation.board(updatedState), Some(updatedState))
    }
  }

  private def onFastBackwardIssue(who: String, ref: String, currentState: Model) = {
    currentState.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newStatus = None
      val updated = found.copy(status = newStatus, by = None)
      val index = currentState.issues.indexOf(found)
      val updatedState = currentState.copy(issues = currentState.issues.updated(index, updated))
      Out(Presentation.board(updatedState), Some(updatedState))
    }
  }

  private def onForwardIssue(who: String, ref: String, currentState: Model) = {
    currentState.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newStatus = if (found.status.isEmpty) currentState.workflowStates.head
      else {
        val currentIndex = currentState.workflowStates.indexOf(found.status.get)
        val newIndex = if (currentIndex >= currentState.workflowStates.size - 1) currentIndex else currentIndex + 1
        currentState.workflowStates(newIndex)
      }
      val updated = found.copy(status = Some(newStatus), by = Some(currentState.userToAka(who)))
      val index = currentState.issues.indexOf(found)
      val updatedState = currentState.copy(issues = currentState.issues.updated(index, updated))
      Out(Presentation.board(updatedState), Some(updatedState))
    }
  }

  private def onFastForwardIssue(who: String, ref: String, currentState: Model) = {
    currentState.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newStatus = currentState.workflowStates.reverse.head
      val updated = found.copy(status = Some(newStatus), by = Some(currentState.userToAka(who)))
      val index = currentState.issues.indexOf(found)
      val updatedState = currentState.copy(issues = currentState.issues.updated(index, updated))
      Out(Presentation.board(updatedState), Some(updatedState))
    }
  }

  private def onEditIssue(ref: String, args: List[String], currentState: Model) = {
    currentState.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newDescription = args.mkString(" ")
      val updated = found.copy(description = newDescription)
      val index = currentState.issues.indexOf(found)
      val updatedState = currentState.copy(issues = currentState.issues.updated(index, updated))
      Out(s"= ${updated.render}" :: Nil, Some(updatedState))
    }
  }

  private def onRemoveIssue(ref: String, currentState: Model) = {
    currentState.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val updatedState = currentState.copy(issues = currentState.issues.filterNot(i => i == found))
      Out(s"- ${found.render}" :: Nil, Some(updatedState))
    }
  }

  //TODO: add search to Model
  private def onQueryIssues(currentState: Model, query: Option[String]) = {
    val matching = query.fold(currentState.issues)(q => currentState.issues.filter(i => i.search(q)))
    val result = if (matching.isEmpty) (s"no issues found" + (if (query.isDefined) s" for: ${query.get}" else "")) :: Nil
    else matching.reverse.map(i => i.render)
    Out(result, None)
  }

  private def onAddIssue(args: List[String], currentState: Model) = {
    val newRef = Controller.issueRef.next
    val description = args.mkString(" ")
    val created = Issue(newRef, description, None, None)
    val updatedState = currentState.copy(issues = created :: currentState.issues)
    Out(s"+ ${created.render}" :: Nil, Some(updatedState))
  }

  private def onAka(who: String, aka: String, currentState: Model): Out = {
    if (aka.size > 3) return Out(Messages.problem("maximum 3 chars"), None)
    val updatedState = currentState.copy(userToAka = currentState.userToAka.updated(who, aka.toUpperCase))
    Out(Messages.help(aka.toUpperCase), Some(updatedState))
  }
}

object Presentation {
  def board(state: Model) = {
    val stateToIssues = state.issues.groupBy(_.status)
    state.workflowStates.map(s => {
      val issuesForState = stateToIssues.getOrElse(Some(s), Nil)
      val issues = issuesForState.map(i => s"\n  ${i.render}").mkString
      s"$s: (${issuesForState.size})" + issues + "\n"
    })
  }
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
        out.updatedState.map(s => {
          model = s
          Persistence.save(model)
        })
        t(out.messages)
      }

      //TODO:
      //release
      //check for dupes when adding ...
      //log all commands somewhere
      //ref # tag
      //ref #- de-tag
      //# show tags
    })

  //TODO: this should exclude me ...
  def knows_?(who: String) = model.userToAka.contains(who)
}

object Persistence {
  private val file = new File("rim.json")
  private val defaultStatuses = List("next", "doing", "done")

  def load: Model = {
    if (!file.exists()) save(Model(defaultStatuses, immutable.Map[String, String](), List[Issue]()))
    Json.deserialise(Source.fromFile(file).getLines().mkString("\n"))
  }

  def save(state: Model) {
    Files.write(Paths.get(file.getName), pretty(render(Json.serialise(state))).getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
  }
}

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

