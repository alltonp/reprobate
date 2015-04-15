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

//TODO: colourise
//http://stackoverflow.com/questions/287871/print-in-terminal-with-colors-using-python?rq=1
//http://apple.stackexchange.com/questions/74777/echo-color-coding-stopped-working-in-mountain-lion
//http://unix.stackexchange.com/questions/43408/printing-colored-text-using-echo
//e.g. printf '%s \e[0;31m%s\e[0m %s\n' 'Some text' 'in color' 'no more color'
//TODO: support curl
//#MESSAGE="(Foo) Deployed ${VERSION} to ${MACHINE_NAME}"
//#curl --connect-timeout 15 -H "Content-Type: application/json" -d "{\"message\":\"${MESSAGE}\"}" http://localhost:8765/broadcast
//#wget --timeout=15 --no-proxy -O- --post-data="{\"message\":\"${MESSAGE}\"}" --header=Content-Type:application/json "http://localhost:8765/broadcast"
object Messages {
  val eh = "- eh?"
  val ok = "- ok"
//  def red(value: String) = s"\e[1;31m $value \e[0m"

  def notAuthorised(who: String) = List(s"- easy ${who}, please set your initials first ⇒ 'rim aka pa'") //s"OK - ${who} is ${key} ${value}"

  def help(who: String) = List(
    s"hello ${who}, welcome to rim - rudimental issue management © 2015 spabloshi ltd",
    "- set an aka ⇒ 'rim aka [initials]'",
    "- add issue ⇒ 'rim + [the description]'",
    "- list issues ⇒ 'rim ? {query}'",
    "- delete issue ⇒ 'rim [ref] -'",
    "- move issue forward ⇒ 'rim [ref] /'",
    "- move issue backward ⇒ 'rim [ref] .'",
    "- own issue ⇒ 'rim [ref] @'",
    "- display this message ⇒ 'rim help'"
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

case class RimState(workflowStates: List[String], userToAka: immutable.Map[String, String], issues: List[Issue])

case class In(head: Option[String], tail:List[String])
case class Out(response: Box[LiftResponse], updatedState: Option[RimState])

object Commander {

  //TODO: this return is definitely not right ....
  def process(cmd: In, who: String, currentState: RimState): Out = {
    if (!cmd.head.getOrElse("").equals("aka") && !Controller.knows_?(who)) return Out(t(Messages.notAuthorised(who)), None)

    cmd match {
      case In(Some("aka"), List(aka)) => onAka(who, aka, currentState)
      case In(Some("+"), args) => onAddIssue(args, currentState)
      case In(Some("?"), Nil) => onQueryIssues(currentState)
      case In(Some("?"), List(query)) => onQueryIssuesWithString(currentState, query)
      case In(Some(ref), List("-")) => onRemoveIssue(ref, currentState)
      case In(Some(ref), List("/")) => onForwardIssue(who, ref, currentState)
      case In(Some(ref), List(".")) => onBackwardIssue(who, ref, currentState)
      case In(Some(ref), List("@")) => onOwnIssue(who, ref, currentState)
      case In(Some("help"), Nil) => ooHelp(who)
      case In(Some(""), Nil) => onShowBoard(currentState)
      case In(head, tail) => onUnknownCommand(head, tail)
    }
  }

  private def onUnknownCommand(head: Option[String], tail: List[String]) =
    Out(t(Messages.eh + " " + head.getOrElse("") + " " + tail.mkString(" ") :: Nil), None)

  private def onShowBoard(currentState: RimState) = Out(Presentation.board(currentState), None)

  private def ooHelp(who: String) = Out(t(Messages.help(who)), None)

  private def onOwnIssue(who: String, ref: String, currentState: RimState) = {
    val found = currentState.issues.find(_.ref == ref)
    if (found.isDefined) {
      val updated = found.get.copy(by = Some(currentState.userToAka(who)))
      val index = currentState.issues.indexOf(found.get)
      val updatedState = currentState.copy(issues = currentState.issues.updated(index, updated))
      Out(t(s"@ ${found.get.render}" :: Nil), Some(updatedState))
    } else {
      Out(t(Messages.eh + " " + ref :: Nil), None)
    }
  }

  private def onBackwardIssue(who: String, ref: String, currentState: RimState) = {
    val found = currentState.issues.find(_.ref == ref)
    if (found.isDefined) {
      val newStatus = if (found.get.status.isEmpty) None
      else {
        val currentIndex = currentState.workflowStates.indexOf(found.get.status.get)
        if (currentIndex <= 0) None else Some(currentState.workflowStates(currentIndex - 1))
      }
      val updated = found.get.copy(status = newStatus, by = Some(currentState.userToAka(who)))
      val index = currentState.issues.indexOf(found.get)
      val updatedState = currentState.copy(issues = currentState.issues.updated(index, updated))
      Out(Presentation.board(updatedState), Some(updatedState))
    } else {
      Out(t(Messages.eh + " " + ref :: Nil), None)
    }
}

  private def onForwardIssue(who: String, ref: String, currentState: RimState) = {
    val found = currentState.issues.find(_.ref == ref)
    if (found.isDefined) {
      val newStatus = if (found.get.status.isEmpty) currentState.workflowStates.head
      else {
        val currentIndex = currentState.workflowStates.indexOf(found.get.status.get)
        val newIndex = if (currentIndex >= currentState.workflowStates.size - 1) currentIndex else currentIndex + 1
        currentState.workflowStates(newIndex)
      }
      val updated = found.get.copy(status = Some(newStatus), by = Some(currentState.userToAka(who)))
      val index = currentState.issues.indexOf(found.get)
      val updatedState = currentState.copy(issues = currentState.issues.updated(index, updated))
      Out(Presentation.board(updatedState), Some(updatedState))
    } else {
      Out(t(Messages.eh + " " + ref :: Nil), None)
    }
  }

  private def onRemoveIssue(ref: String, currentState: RimState) = {
    val found = currentState.issues.find(_.ref == ref)
    if (found.isDefined) {
      val updatedState = currentState.copy(issues = currentState.issues.filterNot(i => i == found.get))
      Out(t(s"- ${found.get.render}" :: Nil), Some(updatedState))
    } else {
      Out(t(Messages.eh + " " + ref :: Nil), None)
    }
  }

  //TODO: combine
  private def onQueryIssuesWithString(currentState: RimState, query: String) = {
    val matching = currentState.issues.filter(i => i.search(query))
    val result = if (matching.isEmpty) s"no issues found for: $query" :: Nil
    else matching.reverse.map(i => i.render)
    Out(t(result), None)
  }

  //TODO: combine
  private def onQueryIssues(currentState: RimState) = {
    val matching = currentState.issues
    val result = if (matching.isEmpty) "no issues found" :: Nil
    else matching.reverse.map(i => i.render)
    Out(t(result), None)
  }

  private def onAddIssue(args: List[String], currentState: RimState) = {
    val ref = Controller.issueRef.next
    val description = args.mkString(" ")
    val created = Issue(ref, description, None, None)
    val updatedState = currentState.copy(issues = created :: currentState.issues)
    Out(t(s"+ ${created.render}" :: Nil), Some(updatedState))
  }

  private def onAka(who: String, aka: String, currentState: RimState) = {
    val updatedState = currentState.copy(userToAka = currentState.userToAka.updated(who, aka.toUpperCase))
    Out(t(Messages.help(aka.toUpperCase)), Some(updatedState))
  }
}

object Presentation {
  def board(state: RimState) = {
    val stateToIssues = state.issues.groupBy(_.status)
    val r = state.workflowStates.map(s => {
      val issuesForState = stateToIssues.getOrElse(Some(s), Nil)
      val issues = issuesForState.map(i => s"\n  ${i.render}").mkString
      s"$s: (${issuesForState.size})" + issues
    })
    t(r)
  }
}

object Controller {
  private var state = Persistence.load
  val issueRef = IssueRef(if (state.issues.isEmpty) 0 else state.issues.map(_.ref).max.toLong)

  def process(who: String, req: Req): Box[LiftResponse] =
    JsonRequestHandler.handle(req)((json, req) ⇒ {
      val value = RimRequestJson.deserialise(pretty(render(json))).value.toLowerCase.trim
      println(s"=> $who: [${value}]")
      val bits = value.split(" ").map(_.trim)
      val cmd = In(bits.headOption, bits.tail.toList)

      synchronized {
        val out = Commander.process(cmd, who, state)
        out.updatedState.map(s => {
          state = s
          Persistence.save(state)
        })
        out.response
      }

      //TODO: next ..
      //id //
      //id !!
      //id = x
      //release
      //check for dupes when adding ...
    })

  //TODO: this should exclude me ...
  def knows_?(who: String) = state.userToAka.contains(who)
}

object Persistence {
  private val file = new File("rim.json")
  private val defaultStatuses = List("next", "doing", "done")

  def load: RimState = {
    if (!file.exists()) save(RimState(defaultStatuses, immutable.Map[String, String](), List[Issue]()))
    Json.deserialise(Source.fromFile(file).getLines().mkString("\n"))
  }

  def save(state: RimState) {
    Files.write(Paths.get(file.getName), pretty(render(Json.serialise(state))).getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
  }
}

//TODO: protect against empty value
//TODO: discover common keys and present them when updating
//TODO: be careful with aka .. they need to be unique
//TODO: on update, don't show self in list of others and don't show anything if others are empty
//TODO: make it possible to ask questions and force others to answer them
