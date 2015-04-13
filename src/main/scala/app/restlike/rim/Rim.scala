package app.restlike.rim

import app.restlike.iam.Iam._
import net.liftweb.http.rest.RestHelper
import net.liftweb.http._
import net.liftweb.common._
import net.liftweb.json._
import scala.collection.{immutable, mutable}
import java.nio.file.{StandardOpenOption, Paths, Files}
import java.nio.charset.StandardCharsets
import java.io.File
import scala.io.Source
import scala.collection
import net.liftweb.common.Full
import scala.Some

object Rim extends RestHelper {
  import Responder._
  import Messages._

  serve {
    case r@Req("rim" :: "install" :: Nil, _, GetRequest) ⇒ () ⇒ t(install, downcase = false)
    case r@Req("rim" :: who :: Nil, _, PostRequest) ⇒ () ⇒ Model.update(who, r)
    case _ ⇒ t("sorry, it looks like your rim is badly configured" :: Nil)
  }
}

object Responder {
  def t(messages: List[String], downcase: Boolean = true) = {
    val response = messages.mkString("\n")
    println("=> " + response)
    Full(PlainTextResponse(if (downcase) response.toLowerCase else response))
  }
}

object Messages {
  val eh = "- eh?"
  val ok = "- ok"

  def notAuthorised(who: String) = List(s"- easy ${who}, please set your initials first ⇒ 'rim aka pa'") //s"OK - ${who} is ${key} ${value}"

  def help(who: String) = List(
    s"- hello ${who}, welcome to rim! - © 2015 spabloshi ltd",
    "",
    "- to display this message ⇒ 'rim help'"
  )

  //TODO: parameterise the hostname and proxy bits and load from disk
  val install =
    """#!/bin/bash
      |#INSTALLATION:
      |#- alias rim='{path to}/rim.sh'
      |#- set RIM_HOST (e.g. RIM_HOST="http://localhost:8473")
      |#- that's it!
      |
      |#RIM_HOST="http://localhost:8473"
      |OPTIONS="--timeout=15 --no-proxy -qO-"
      |WHO=`id -u -n`
      |BASE="rim/$WHO"
      |REQUEST="$OPTIONS $RIM_HOST/$BASE"
      |MESSAGE="${@:1}"
      |RESPONSE=`wget $REQUEST --post-data="{\"value\":\"${MESSAGE}\"}" --header=Content-Type:application/json`
      |echo "$RESPONSE"
      |
    """.stripMargin.split("\n").toList
}

object JsonRequestHandler extends Loggable {
  import Responder._

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

case class RimState(rim: immutable.Map[String, immutable.Map[String, String]])
case class RimUpdate(value: String)

object Model {
  import Responder._
  import Messages._

  private val file = new File("rim.json")
  private val whoToStatuses = load

  println("### loaded:" + whoToStatuses)

  def query(who: String, key: Option[String]) = t(
    if (Model.knows_?(who)) key.fold(allAboutEveryone){k => aboutEveryone(k)}
    else key.fold(help(who)){k => notAuthorised(who) }
  )

  //TODO:
  //(1) get aka and store in list of key-value or map!
  //(2) get issues and store in map by (atomic) id
  def update(who: String, req: Req): Box[LiftResponse] =
    JsonRequestHandler.handle(req)((json, req) ⇒ {
      val value = RimRequestJson.deserialise(pretty(render(json))).value.trim
      val bits = value.split(" ")
      println(s"message in: ${bits.toList}")

      if (!Model.knows_?(who)) return t(notAuthorised(who))

      //TODO: need to check that there are args - for pretty much all (except help)
      bits.headOption match {
        case Some("aka") => t(s"akaing: " + bits.init.mkString(" ") :: Nil)
        case Some("+") => t(s"adding: " + bits.init.mkString(" ") :: Nil)
        case Some("help") => t(help(who))
        //>
        //<
        //?
        case Some(x) => t(s"$eh $x" :: Nil)
        case None => t(eh :: Nil) //TODO: should be help
      }

////      safeDoUpdate(who, key, value)
////      t("- ok, " + who + " is now " + allAbout(who) :: aboutEveryone(key))
//      t(value :: Nil)
    })

  def delete(who: String) = {
    safeDoUpdate(who, null, null, delete = true)
    t("- ok, " + who + " has now left the building" :: allAboutEveryone)
  }

  private def allAboutEveryone = everyone.map(w => "- " + w + " is " + allAbout(w) ).toList
  private def allAbout(who: String) = whoToStatuses(who).keys.to.sorted.map(k => k + " " + whoToStatuses(who)(k)).mkString(", ")

  //TODO: this should exclude me ...
  private def aboutEveryone(key: String) = everyone.map(w => "- " + w + " is " + key + " " + whoToStatuses(w).getOrElse(key, "???") ).toList
  private def everyone = whoToStatuses.keys.toList.sorted
  private def knows_?(who: String) = whoToStatuses.contains(who)
  private def keysFor(who: String) = if (!whoToStatuses.contains(who)) mutable.Map.empty[String, String] else whoToStatuses(who)

  private def safeDoUpdate(who: String, key: String, value: String, delete: Boolean = false) {
    def updateKey(who: String, key: String, value: String) {
      val state = keysFor(who)
      val newState: immutable.Map[String, String] = state.updated(key, value).toMap
      whoToStatuses.update(who, newState)
    }

    def deleteKey(who: String, key: String) {
      val state = keysFor(who)
      val newState = state.-(key).toMap
      whoToStatuses.update(who, newState)
    }

    def deleteAll(who: String) { whoToStatuses.remove(who) }

    synchronized {
      if (delete) deleteAll(who)
      else if ("-" == value.trim) deleteKey(who, key)
      else updateKey(who, key, value)
      save(RimState(whoToStatuses.toMap))
    }
  }

  def load: mutable.Map[String, immutable.Map[String, String]] = {
    if (!file.exists()) save(RimState(immutable.Map[String, immutable.Map[String, String]]()))
    val raw = Json.deserialise(Source.fromFile(file).getLines().mkString("\n")).rim
    if (raw.isEmpty) mutable.Map[String, immutable.Map[String, String]]()
    else collection.mutable.Map(raw.toSeq: _*)
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
