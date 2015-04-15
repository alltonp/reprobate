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

case class Issue(ref: String, description: String, state: Option[String], by: Option[String]) {
  private val indexed = List(ref, description, state.getOrElse("")).mkString(" ")

  def search(query: String) = indexed.contains(query)
  //TODO: ${state.fold("")(":" + _)}
  def render = s"$ref: $description ${by.fold("")("@" + _.toUpperCase)}"
}

case class RimState(workflowStates: List[String], userToAka: immutable.Map[String, String], issues: List[Issue])

object Commander {

  //TODO: this return is definitely not right ....
  def process(cmd: Cmd, who: String, currentState: RimState): Box[LiftResponse] = {
    if (!cmd.head.getOrElse("").equals("aka") && !Controller.knows_?(who)) return t(Messages.notAuthorised(who))

    cmd match {
      case Cmd(Some("aka"), List(aka)) => doAka(who, aka, currentState)
      case Cmd(Some("+"), args) => doAddIssue(args, currentState)
      case Cmd(Some("?"), Nil) => doQuery(currentState)
      case Cmd(Some("?"), List(query)) => doQueryWithString(currentState, query)
      case Cmd(Some(ref), List("-")) => doRemoveIssue(ref, currentState)
      case Cmd(Some(ref), List("/")) => doForwardIssue(who, ref, currentState)
      case Cmd(Some(ref), List(".")) => doBackwardIssue(who, ref, currentState)
      case Cmd(Some(ref), List("@")) => doOwnIssue(who, ref, currentState)
      case Cmd(Some("help"), Nil) => doHelp(who)
      case Cmd(Some(""), Nil) => doShowBoard(currentState)
      case Cmd(head, tail) => doUnknownCommand(head, tail)
    }
  }

  def doUnknownCommand(head: Option[String], tail: List[String]): Full[PlainTextResponse] = {
    t(Messages.eh + " " + head.getOrElse("") + " " + tail.mkString(" ") :: Nil)
  }

  def doShowBoard(currentState: RimState): Full[PlainTextResponse] = {
    Present.board(currentState)
  }

  def doHelp(who: String): Full[PlainTextResponse] = {
    t(Messages.help(who))
  }

  def doOwnIssue(who: String, ref: String, currentState: RimState): Full[PlainTextResponse] = {
    val found = currentState.issues.find(_.ref == ref)
    if (found.isDefined) {
      val updated = found.get.copy(by = Some(currentState.userToAka(who)))
      val index = currentState.issues.indexOf(found.get)
      Controller.state = currentState.copy(issues = currentState.issues.updated(index, updated))
      Persistence.save(Controller.state)
      t(s"@ ${found.get.render}" :: Nil)
    } else {
      t(Messages.eh + " " + ref :: Nil)
    }
  }

  def doBackwardIssue(who: String, ref: String, currentState: RimState): Full[PlainTextResponse] = {
    val found = currentState.issues.find(_.ref == ref)
    if (found.isDefined) {
      val nextState = if (found.get.state.isEmpty) None
      else {
        val currentIndex = currentState.workflowStates.indexOf(found.get.state.get)
        if (currentIndex <= 0) None else Some(currentState.workflowStates(currentIndex - 1))
      }
      val updated = found.get.copy(state = nextState, by = Some(Controller.state.userToAka(who)))
      val index = currentState.issues.indexOf(found.get)
      Controller.state = currentState.copy(issues = currentState.issues.updated(index, updated))
      Persistence.save(Controller.state)
      Present.board(Controller.state)
    } else {
      t(Messages.eh + " " + ref :: Nil)
    }
}

  def doForwardIssue(who: String, ref: String, currentState: RimState): Full[PlainTextResponse] = {
    val found = currentState.issues.find(_.ref == ref)
    if (found.isDefined) {
      val nextState = if (found.get.state.isEmpty) currentState.workflowStates.head
      else {
        val currentIndex = currentState.workflowStates.indexOf(found.get.state.get)
        val newIndex = if (currentIndex >= currentState.workflowStates.size - 1) currentIndex else currentIndex + 1
        currentState.workflowStates(newIndex)
      }
      val updated = found.get.copy(state = Some(nextState), by = Some(currentState.userToAka(who)))
      val index = currentState.issues.indexOf(found.get)
      Controller.state = currentState.copy(issues = currentState.issues.updated(index, updated))
      Persistence.save(Controller.state)
      Present.board(Controller.state)
    } else {
      t(Messages.eh + " " + ref :: Nil)
    }
  }

  def doRemoveIssue(ref: String, currentState: RimState): Full[PlainTextResponse] = {
    val found = currentState.issues.find(_.ref == ref)
    if (found.isDefined) {
      Controller.state = currentState.copy(issues = currentState.issues.filterNot(i => i == found.get))
      Persistence.save(Controller.state)
      t(s"- ${found.get.render}" :: Nil)
    } else {
      t(Messages.eh + " " + ref :: Nil)
    }
  }

  //TODO: combine
  def doQueryWithString(currentState: RimState, query: String): Full[PlainTextResponse] = {
    val matching = currentState.issues.filter(i => i.search(query))
    val result = if (matching.isEmpty) s"no issues found for: $query" :: Nil
    else matching.reverse.map(i => i.render)
    t(result)
  }

  //TODO: combine
  def doQuery(currentState: RimState): Full[PlainTextResponse] = {
    val matching = currentState.issues
    val result = if (matching.isEmpty) "no issues found" :: Nil
    else matching.reverse.map(i => i.render)
    t(result)
  }

  def doAddIssue(args: List[String], currentState: RimState): Full[PlainTextResponse] = {
    val ref = Controller.issueRef.next
    val description = args.mkString(" ")
    val created = Issue(ref, description, None, None)
    Controller.state = currentState.copy(issues = created :: currentState.issues)
    Persistence.save(Controller.state)
    t(s"+ ${created.render}" :: Nil)
  }

  def doAka(who: String, aka: String, state: RimState): Full[PlainTextResponse] = {
    Controller.state = state.copy(userToAka = state.userToAka.updated(who, aka.toUpperCase))
    Persistence.save(Controller.state)
    t(Messages.help(aka.toUpperCase))
  }
}

case class Cmd(head: Option[String], tail:List[String])

object Present {
  def board(state: RimState) = {
    val stateToIssues = state.issues.groupBy(_.state)
    val r = state.workflowStates.map(s => {
      val issuesForState = stateToIssues.getOrElse(Some(s), Nil)
      val issues = issuesForState.map(i => s"\n  ${i.render}").mkString
      s"$s: (${issuesForState.size})" + issues
    })
    t(r)
  }
}

object Controller {
  var state = Persistence.load
  val issueRef = IssueRef(if (state.issues.isEmpty) 0 else state.issues.map(_.ref).max.toLong)

  println("### loaded:" + state)

  def process(who: String, req: Req): Box[LiftResponse] =
    JsonRequestHandler.handle(req)((json, req) ⇒ {
      val value = RimRequestJson.deserialise(pretty(render(json))).value.toLowerCase.trim
      println(s"=> $who: [${value}]")

      val bits = value.split(" ").map(_.trim)
      val cmd = Cmd(bits.headOption, bits.tail.toList)

      //TODO: feels like this should take the state and return an update state (option) and presentation
      //TODO: we can just sycronise around the whole thing
      synchronized {
        Commander.process(cmd, who, state)
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
