package app.restlike.rem

import java.io.Serializable
import java.nio.file.Paths

import app.restlike.common._
import Colours._
import app.restlike.common.Responder._
import im.mange.little.file.Filepath
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.json._
import org.joda.time.DateTime

import scala.collection.immutable

//TODO: Next
//update key
//update value
//tag
//detag
//other standard tagging stuff
//stop using white .. it won't play well on white backgrounds - could have an inverse user setting?

//TODO: Then
//store things per user (think about globlal id vs user id)
//tidy up everything about status, ownership and releases

//TODO: could it be more about creating entities and add KV pairs to them
//TODO: implememt slack style tokens

//TODO: de-dupe
//pull out a clitools jar
//can we share serialisers and persistence? (put in common) .. might be hard



case class Thing(ref: String, key: String, value: Option[String], tags: Set[String] = Set.empty/*, history: Seq[History] = Seq.empty*/) {
  val description = s"$key = $value"
//  description: String, status: Option[String], by: Option[String]
//  private val renderBy = by.fold("")(" @" + _)
  private val renderTags = tags.toList.sorted.map(t => s" :$t").mkString
//  private val renderStatus = status.fold("")(" ^" + _)
  private val indexed = List(ref/*, description, renderStatus, renderBy.toLowerCase,*/, key, value.getOrElse(""), renderTags).mkString(" ")

  def search(query: String) = indexed.contains(query)
  def render() = s"${lightGrey(s"$ref:")} ${orange(key)}${value.fold("")(v => s" ${lightGrey("=")} ${cyan(v)}")}${lightGrey(renderTags)}"
}

//case class History(who: String, command: String)

//case class Release(tag: String, issues: List[Thing])

case class IssueCreation(created: Thing, updatedModel: Model)

case class Tag(name: String, count: Int)

case class Model(/*workflowStates: List[String],*/ userToAka: immutable.Map[String, String], things: List[Thing]/*, released: List[Release]*/) {
  def knows_?(who: String) = userToAka.contains(who)

  def createThing(args: List[String], status: Option[String], by: Option[String], refProvider: RefProvider): Either[List[String], IssueCreation] = {
    if (args.mkString("").trim.isEmpty) return Left(Messages.descriptionEmpty)

    //TODO: this is well shonky!
    var descriptionBits = List.empty[String]
    var tagBits = List.empty[String]
    var tagging = false

    args.foreach(a => {
      if (a == ":") tagging = true
      else {
        if (tagging) tagBits = a.replaceAll(":", "") :: tagBits
        else descriptionBits = a :: descriptionBits
      }
    })

    val description = descriptionBits.reverse.mkString(" ")
    val keyValueBits = description.split(" = ").map(_.trim)
    //TODO: validate there is min 1
    //TODO: validate there is max 2
    //TODO: validate key is not already used
    val maybeDupe = things.find(i => i.description == description)
    if (maybeDupe.isDefined) return Left(Messages.duplicateIssue(maybeDupe.get.ref))
    val newRef = refProvider.next
    val created = Thing(newRef, keyValueBits.head, keyValueBits.drop(1).headOption, tagBits.toSet)
    val updatedModel = this.copy(things = created :: this.things)
    Right(IssueCreation(created, updatedModel))
  }

  def updateIssue(updated: Thing) = {
    val index = this.things.indexOf(findIssue(updated.ref).get)
    this.copy(things = this.things.updated(index, updated))
  }

  def aka(who: String) = userToAka(who)
  def akas = userToAka.values.toList.distinct
  def findIssue(ref: String) = things.find(_.ref == ref)
//  def beginState = workflowStates.head
//  def state(number: Int) = workflowStates(number) //TODO: this obviously needs thinking about if the states change
//  def endState = workflowStates.reverse.head
//  def releasableIssues = things.filter(_.status == Some(endState))
//  def releaseTags = released.map(_.tag)

  def tags = {
    val allIssues = /*released.map(_.issues).flatten ++*/ things
    val allTheTags = allIssues.map(_.tags).flatten
    val uniqueTags = allTheTags.distinct
    uniqueTags.map(t => Tag(t, allTheTags.count(_ == t)))
  }
}

case class In(head: Option[String], tail:List[String])
case class Out(messages: List[String] = Nil, updatedModel: Option[Model] = None)

//TODO: add issue
object Presentation {
//  def board(model: Model) = {
//    val stateToIssues = model.things.groupBy(_.status)
//    model.workflowStates.map(s => {
//      val issuesForState = stateToIssues.getOrElse(Some(s), Nil)
//      val issues = issuesForState.map(i => s"\n  ${i.render(hideStatus = true)}").mkString
//      s"$s: (${issuesForState.size})" + issues + "\n"
//    })
//  }
  
//  def release(release: Release) = {
//    val r = release.issues.map(i => s"\n  ${i.render(hideStatus = true)}").mkString
//    s"${release.tag}: (${release.issues.size})" + r + "\n"
//  }

//  def issuesForUser(aka: String, issues: List[Thing]) = {
//    val r = issues.map(i => s"\n  ${i.render(hideBy = true)}").mkString
//    s"${aka}: (${issues.size})" + r + "\n"
//  }

  def tags(all: Seq[Tag]) = {
    val sorted = all.sortBy(t => (-t.count, t.name)).map(t => s"${t.name} (${t.count})")
    ": " + sorted.mkString(", ") :: Nil
  }
}

//TODO: handle corrupted rem.json

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

///Json

import net.liftweb.common.{Full, Box, Loggable}
import net.liftweb.http.{LiftResponse, Req}
import net.liftweb.json.JsonAST

object Json {
  import net.liftweb.json.Serialization._
  import net.liftweb.json._

  private val iamFormats = Serialization.formats(NoTypeHints)

  def deserialise(json: String) = {
    implicit val formats = iamFormats
    parse(json).extract[Model]
  }

  def serialise(response: Model) = {
    implicit val formats = iamFormats
    JsonParser.parse(write(response))
  }
}

import net.liftweb.common.Full
import net.liftweb.http.rest.RestHelper
import net.liftweb.http._

object Rem extends RestHelper {
  import app.restlike.rem.Messages._
  import app.restlike.common.Responder._

  //TODO: extract app name
  serve {
    case r@Req("rem" :: "install" :: Nil, _, GetRequest) ⇒ () ⇒ t(Script.install("rem"), downcase = false)
    case r@Req("rem" :: "tracking" :: Nil, _, GetRequest) ⇒ () ⇒ t(Tracker("rem.tracking").view, downcase = false)
    case r@Req("rem" :: "state" :: Nil, _, GetRequest) ⇒ () ⇒ Full(JsonResponse(Json.serialise(Persistence.load)))
    case r@Req("rem" :: who :: Nil, _, PostRequest) ⇒ () ⇒ Controller.process(who, r)
  }
}
