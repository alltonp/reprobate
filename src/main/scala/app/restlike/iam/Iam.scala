package app.restlike.iam

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

//TODO: simplifiy all of this:
//POST IAM payload ... and figure out form there what it is ..
object Iam extends RestHelper {
  import Responder._
  import Messages._

  serve {
    case r@Req("iam" :: "install" :: Nil, _, GetRequest) ⇒ () ⇒ t(install, downcase = false)
    case r@Req("iam" :: who :: "help" :: Nil, _, GetRequest) ⇒ () ⇒ t(help(who))
    case r@Req("iam" :: who :: Nil, _, GetRequest) ⇒ () ⇒ Model.query(who, None)
    //TODO: experimental
    case r@Req("iam" :: who :: "+" :: Nil, _, PostRequest) ⇒ () ⇒ t(List(s"$who wants to + " + r.json))
    case r@Req("iam" :: who :: "-" :: Nil, _, GetRequest) ⇒ () ⇒ Model.delete(who) // a little odd that this is a GET, but we will get over it
    case r@Req("iam" :: who :: key :: Nil, _, GetRequest) ⇒ () ⇒ Model.query(who, Some(key))
    case r@Req("iam" :: who :: key :: Nil, _, PostRequest) ⇒ () ⇒ Model.update(who, key, r)
    //case _ ⇒ t(eh)
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
  val eh = List("- eh?")
  val ok = "ok"

  def notAuthorised(who: String) = List(s"- easy ${who}, you must share before you can query, see 'iam help'") //s"OK - ${who} is ${key} ${value}"

  def help(who: String) = List(
   s"- hello ${who}, welcome to iam! - © 2014 flatmap ltd",
    "",
    "- what is iam?",
    "  probably the *only* micro-status social network!",
    "",
    "- right, er ... so what would i use it for?",
    "  sharing information about yourself",
    "  seeing what information others have shared about themselves",
    "",
    "- ok, how does it work?",
    "  essentially you set micro statuses, associating a 'key' with 'value' ⇒ 'iam [doing] [task x y z]'",
    "  where the key is 'doing' and value is 'task x y z'",
    "  you can then query what other users are doing ⇒ 'iam doing'",
    "  keys are completely free-range, over time domain specific keys will emerge (probably)",
    "",
    "- amazing, how do i get started?",
    "  you *must* first share something about yourself e.g. 'iam doing support today'",
    "",
    "- after that feel free to add/update/delete/query whatever statuses your heart desires, using:",
    "  set (or update) a status ⇒ 'iam [key] [value blah blah blah]'",
    "  query a status ⇒ 'iam [key]'",
    "  remove a status ⇒ 'iam' [key] -",
    "  query all statuses ⇒ 'iam'",
    "  remove yourself completely  ⇒ 'iam' -",
    "",
    "- for example:",
   s"  is '${who}' your real name?, why not set an aka ⇒ 'iam aka [name]`",
    "  set where you are located ⇒ 'iam in london'",
    "  set where you are working ...",
    "    'iam at home' or ",
    "    'iam working from home' or",
    "    'iam @ home'",
    "  ... literally anything you like ...",
    "    'iam at lunch'",
    "    'iam on holiday until x'",
    "    'iam feeling happy'",
    "    'iam leaving at five'",
    "    'iam doing T123'",
    "    'iam pairing-with barry'",
    "",
    "- to display this message ⇒ 'iam help'"
  )

  //TODO: parameterise the hostname and proxy bits and load from disk
  val install =
    """#!/bin/bash
      |#INSTALLATION:
      |#- alias iam='{path to}/iam.sh'
      |#- set IAM_HOST (e.g. IAM_HOST="http://localhost:8473")
      |#- that's it!
      |
      |OPTIONS="--timeout=15 --no-proxy -qO-"
      |WHO=`id -u -n`
      |BASE="iam/$WHO"
      |REQUEST="$OPTIONS $IAM_HOST/$BASE"
      |
      |#set
      |if [[ $# > 1 ]]; then
      |    REQUEST="$REQUEST/$1"
      |    MESSAGE="${@:2}"
      |    RESPONSE=`wget $REQUEST --post-data="{\"value\":\"${MESSAGE}\"}" --header=Content-Type:application/json`
      |#get
      |else
      |   if [[ $# > 0 ]]; then
      |     REQUEST="$REQUEST/$1"
      |   fi
      |  RESPONSE=`wget $REQUEST`
      |fi
      |
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

object IamRequestJson {
  import net.liftweb.json._

  def deserialise(json: String) = {
    implicit val formats = Serialization.formats(NoTypeHints)
    parse(json).extract[IamUpdate]
  }
}

case class IamState(iam: immutable.Map[String, immutable.Map[String, String]])
case class IamUpdate(value: String)

object Model {
  import Responder._
  import Messages._

  private val file = new File("data/iam.json")
  private val whoToStatuses = load

  println("### loaded:" + whoToStatuses)

  def query(who: String, key: Option[String]) = t(
    if (Model.knows_?(who)) key.fold(allAboutEveryone){k => aboutEveryone(k)}
    else key.fold(help(who)){k => notAuthorised(who) }
  )

  def update(who: String, key: String, req: Req) =
    JsonRequestHandler.handle(req)((json, req) ⇒ {
      val value = IamRequestJson.deserialise(pretty(render(json))).value
      safeDoUpdate(who, key, value)
      t("- ok, " + who + " is now " + allAbout(who) :: aboutEveryone(key))
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
      save(IamState(whoToStatuses.toMap))
    }
  }

  def load: mutable.Map[String, immutable.Map[String, String]] = {
    if (!file.exists()) save(IamState(immutable.Map[String, immutable.Map[String, String]]()))
    val raw = Json.deserialise(Source.fromFile(file).getLines().mkString("\n")).iam
    if (raw.isEmpty) mutable.Map[String, immutable.Map[String, String]]()
    else collection.mutable.Map(raw.toSeq: _*)
  }

  private def save(state: IamState) {
    println("### save: " + state)
    val jsonAst = Json.serialise(state)
    Files.write(Paths.get(file.getPath), pretty(render(jsonAst)).getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
  }
}

object Json {
  import net.liftweb.json.Serialization._
  import net.liftweb.json._

  private val iamFormats = Serialization.formats(NoTypeHints)

  def deserialise(json: String) = {
    implicit val formats = iamFormats
    parse(json).extract[IamState]
  }

  def serialise(response: IamState) = {
    implicit val formats = iamFormats
    JsonParser.parse(write(response))
  }
}

//TODO: protect against empty value
//TODO: discover common keys and present them when updating
//TODO: be careful with aka .. they need to be unique
//TODO: on update, don't show self in list of others and don't show anything if others are empty
//TODO: make it possible to ask questions and force others to answer them
