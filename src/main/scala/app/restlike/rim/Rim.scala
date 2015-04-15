package app.restlike.rim

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths, StandardOpenOption}

import net.liftweb.common.{Full, _}
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper
import net.liftweb.json._

import scala.collection.immutable
import scala.io.Source

object Rim extends RestHelper {
  import app.restlike.rim.Messages._
  import app.restlike.rim.Responder._

  serve {
    case r@Req("rim" :: "install" :: Nil, _, GetRequest) ⇒ () ⇒ t(install, downcase = false)
    case r@Req("rim" :: who :: Nil, _, PostRequest) ⇒ () ⇒ Model.update(who, r)
    case _ ⇒ t("sorry, it looks like your rim is badly configured" :: Nil)
  }
}

object Responder {
  def t(messages: List[String], downcase: Boolean = false) = {
    val response = messages.mkString("\n")
    println("<= " + response)
    Full(PlainTextResponse(if (downcase) response.toLowerCase else response))
  }
}

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

object JsonRequestHandler extends Loggable {
  import app.restlike.rim.Responder._

  def handle(req: Req)(process: (JsonAST.JValue, Req) ⇒ Box[LiftResponse]) = {
    try {
      req.json match {
        case Full(json) ⇒ process(json, req)
        case o ⇒ println(req.json); t(List(s"unexpected item in the bagging area ${o}"))
      }
    } catch {
      case e: Exception ⇒ println("### Error handling request: " + req + " - " + e.getMessage); t(List(e.getMessage))
    }
  }
}

object RimRequestJson {
  import net.liftweb.json._

  def deserialise(json: String) = {
    implicit val formats = Serialization.formats(NoTypeHints)
    parse(json).extract[RimUpdate]
  }
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
case class RimUpdate(value: String)

object Model {
  import app.restlike.rim.Messages._
  import app.restlike.rim.Responder._

  private val file = new File("rim.json")
  private var state = load
  private val issueRef = IssueRef(if (state.issues.isEmpty) 0 else state.issues.map(_.ref).max.toLong)

  println("### loaded:" + state)

//  def query(who: String, key: Option[String]) = t(
//    if (Model.knows_?(who)) key.fold(allAboutEveryone){k => aboutEveryone(k)}
//    else key.fold(help(who)){k => notAuthorised(who) }
//  )

  case class Cmd(head: Option[String], tail:List[String])

  object Present {
    def board = {
      val stateToIssues = state.issues.groupBy(_.state)
      val r = state.workflowStates.map(s => {
        val issuesForState = stateToIssues.getOrElse(Some(s), Nil)
        val issues = issuesForState.map(i => s"\n  ${i.render}").mkString
        s"$s: (${issuesForState.size})" + issues
      })
      t(r)
    }
  }

  def update(who: String, req: Req): Box[LiftResponse] =
    JsonRequestHandler.handle(req)((json, req) ⇒ {
      val value = RimRequestJson.deserialise(pretty(render(json))).value.toLowerCase.trim
      println(s"=> $who: [${value}]")
      val bits = value.split(" ").map(_.trim)
      val cmd = Cmd(bits.headOption, bits.tail.toList)

      if (!cmd.head.getOrElse("").equals("aka") && !Model.knows_?(who)) return t(notAuthorised(who))

      cmd match {
        case Cmd(Some("aka"), List(aka)) => {
          synchronized {
            state = state.copy(userToAka = state.userToAka.updated(who, aka.toUpperCase))
            save(state)
            t(help(aka.toUpperCase()))
          }
        }

        case Cmd(Some("+"), args) => {
          synchronized {
            val ref = issueRef.next
            val description = args.mkString(" ")
            state = state.copy(issues = Issue(ref, description, None, None) :: state.issues)
            save(state)
            t(s"$ref: $description" :: Nil)
          }
        }

        case Cmd(Some("?"), Nil) => {
          val matching = state.issues
          val found = if (matching.isEmpty) "no issues found" :: Nil
                      else matching.reverse.map(i => s"${i.ref}: ${i.description}")
          t(found)
        }

        case Cmd(Some("?"), List(query)) => {
          val matching = state.issues.filter(i => i.search(query))
          val found = if (matching.isEmpty) s"no issues found for: $query" :: Nil
          else matching.reverse.map(i => s"${i.ref}: ${i.description}")
          t(found)
        }

        case Cmd(Some(ref), List("-")) => {
          synchronized {
            val found = state.issues.find(_.ref == ref)
            if (found.isDefined) {
              state = state.copy(issues = state.issues.filterNot(i => i == found.get))
              save(state)
              t(s"deleted: $ref" :: Nil)
            } else {
              t(eh + " " + ref :: Nil)
            }
          }
        }

        case Cmd(Some(ref), List("/")) => {
          synchronized {
            val found = state.issues.find(_.ref == ref)
            if (found.isDefined) {
              val nextState = if (found.get.state.isEmpty) state.workflowStates.head
                              else {
                                val currentIndex = state.workflowStates.indexOf(found.get.state.get)
                                val newIndex = if (currentIndex >= state.workflowStates.size-1) currentIndex else currentIndex + 1
                                state.workflowStates(newIndex)
                              }
              val updated = found.get.copy(state = Some(nextState), by = Some(state.userToAka(who)))
              val index = state.issues.indexOf(found.get)
              state = state.copy(issues = state.issues.updated(index, updated))
              save(state)
              Present.board
            } else {
              t(eh + " " + ref :: Nil)
            }
          }
        }

        case Cmd(Some(ref), List(".")) => {
          synchronized {
            val found = state.issues.find(_.ref == ref)
            if (found.isDefined) {
              val nextState = if (found.get.state.isEmpty) None
                              else {
                                val currentIndex = state.workflowStates.indexOf(found.get.state.get)
                                if (currentIndex <= 0) None else Some(state.workflowStates(currentIndex - 1))
                              }
              val updated = found.get.copy(state = nextState, by = Some(state.userToAka(who)))
              val index = state.issues.indexOf(found.get)
              state = state.copy(issues = state.issues.updated(index, updated))
              save(state)
              Present.board
            } else {
              t(eh + " " + ref :: Nil)
            }
          }
        }

        case Cmd(Some("help"), Nil) => t(help(who))

        case Cmd(Some(""), Nil) => Present.board


        //TODO: should show the current release
        case Cmd(head, tail) => t(eh + " " + head.getOrElse("") + " " + tail.mkString(" ") :: Nil)
      }

      //TODO: next ..
      //id @ (and shoe everywhere)
      //id //
      //id !!
      //id = x
      //release
      //check for dupes when adding ...

////      safeDoUpdate(who, key, value)
////      t("- ok, " + who + " is now " + allAbout(who) :: aboutEveryone(key))
//      t(value :: Nil)
    })

//  def delete(who: String) = {
//    safeDoUpdate(who, null, null, delete = true)
//    t("- ok, " + who + " has now left the building" :: allAboutEveryone)
//  }

//  private def allAboutEveryone = everyone.map(w => "- " + w + " is " + allAbout(w) ).toList
//  private def allAbout(who: String) = whoToStatuses(who).keys.to.sorted.map(k => k + " " + whoToStatuses(who)(k)).mkString(", ")

  //TODO: this should exclude me ...
//  private def aboutEveryone(key: String) = everyone.map(w => "- " + w + " is " + key + " " + whoToStatuses(w).getOrElse(key, "???") ).toList
//  private def everyone = whoToStatuses.keys.toList.sorted
  private def knows_?(who: String) = state.userToAka.contains(who)
//  private def keysFor(who: String) = if (!whoToStatuses.contains(who)) mutable.Map.empty[String, String] else whoToStatuses(who)

//  private def safeDoUpdate(who: String, key: String, value: String, delete: Boolean = false) {
//    def updateKey(who: String, key: String, value: String) {
//      val state = keysFor(who)
//      val newState: immutable.Map[String, String] = state.updated(key, value).toMap
//      whoToStatuses.update(who, newState)
//    }
//
//    def deleteKey(who: String, key: String) {
//      val state = keysFor(who)
//      val newState = state.-(key).toMap
//      whoToStatuses.update(who, newState)
//    }
//
//    def deleteAll(who: String) { whoToStatuses.remove(who) }
//
//    synchronized {
//      if (delete) deleteAll(who)
//      else if ("-" == value.trim) deleteKey(who, key)
//      else updateKey(who, key, value)
//      save(RimState(whoToStatuses.toMap))
//    }
//  }

  def load: RimState = {
    if (!file.exists()) {
      val defaultStatuses = List("next", "doing", "done")
      save(RimState(defaultStatuses, immutable.Map[String, String](), List[Issue]()))
    }
    val raw = Json.deserialise(Source.fromFile(file).getLines().mkString("\n"))
//    if (raw.isEmpty) mutable.Map[String, immutable.Map[String, String]]()
//    else collection.mutable.Map(raw.toSeq: _*)
    raw
  }

  private def save(state: RimState) {
    println("### save: " + state)
    val jsonAst = Json.serialise(state)
    Files.write(Paths.get(file.getName), pretty(render(jsonAst)).getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
  }
}

object Json {
  import net.liftweb.json.Serialization._
  import net.liftweb.json._

  private val iamFormats = Serialization.formats(NoTypeHints)

  def deserialise(json: String) = {
    implicit val formats = iamFormats
    parse(json).extract[RimState]
  }

  def serialise(response: RimState) = {
    implicit val formats = iamFormats
    JsonParser.parse(write(response))
  }
}

//TODO: protect against empty value
//TODO: discover common keys and present them when updating
//TODO: be careful with aka .. they need to be unique
//TODO: on update, don't show self in list of others and don't show anything if others are empty
//TODO: make it possible to ask questions and force others to answer them
