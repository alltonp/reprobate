package app.restlike.rim

import java.io.Serializable
import java.nio.file.Paths

import app.restlike.common._
import app.restlike.common.Colours._
import Responder._
import im.mange.little.file.Filepath
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.json._
import org.joda.time.DateTime

import scala.collection.immutable

//NEXT:
//feedback from team
//Meta tags? Or mark some tags as private or business
//clear screen each time?

//release notes
//- no id, no by and grouped by business tag
//- should be a 'rim note [release]' or 'rim [release] note'

//when doing +/ etc .. show the both the created ref and the new board (or colorise what changed)

//tags:
//show tags by most recent etc (maybe)
//Franck: tag many: `rim ref1 ref2 … refN : foo bar baz`
//tags should be [a-z0-9\-]
//should 'rim [tag] :-' remove tag .. (dicey) .. should be to detag all issues with that tag

//operations to support on many:
//rim 1 2 N .
//rim 1 2 N :
//rim 1 2 N :-

//more view options:
//rim / - show begin
//rim // - show nth state
//rim ! - show end state

//releases:
//store when we released .. useful for really simple tracking

//colouring: (orange = updated, cyan = me, ? = context)
//colorise what changed .. (could be property specific)
//when doing 'rim @' consider highlight what exactly is being done

//gaps:
//properly support multiple / in /// and +///
//properly support multiple . in ...

//dates: (not yet)
//store when released (eek, data change - so make it an option)
//show how long things have been in certain states
//show stats about akas ... entries, last used etc (top 5)
//show how long since aka X updated rim

//query:
//rim . foo => should maybe search like ? does, but just for the backlog for foo ...
//or maybe not because 'and' might cover it ... although how do you search for no status
//rim ? should group by status so we can see what is done, next etc
//why is 'rim .' sorted backwards from 'rim ?'
//rim @ should group by status too (after aka)

//???:
//help should have an 'issues' section for working with multiples on =, : etc
//when doing rim = ... - it's easy to forget the to not copy the tags, seems like tags should be processed (i.e. add)
//might be nice to have rim audit (or track) and see the last x items from the history
//rim @ should sort/breakdown by status, so you can easily what you are doing/have done
//how do we handle rim releases getting too long?

//audit stuff
//might be good to capture who added the issue
//might be good to capture who last updated the issue
//actually if we just store the updates by id then we will get that for free
//store only things that result in a change
//rim [ref] history

//FUTURE:
//- private rims
//- grim
//- spartan bubble ui
//- hosted

//SOMEDAY/MAYBE:
//split and merge {}

//think about:
//would be nice to have a symbol for release ... it could be: ±
//so then show = 'rim ±' or _ .. as in draw a line under it
//so then create = 'rim ± [name]'
//so then notes = 'rim [name] ±' ... need something to differentiate from adding
//so maybe: '_+ [name]' to add, '_' to show, '[name] _' for notes



case class Issue(ref: String, description: String, status: Option[String], by: Option[String], tags: Set[String] = Set.empty/*, history: Seq[History] = Seq.empty*/) {
  private def renderBy(highlightAka: Option[String]) = {
    (by, highlightAka) match {
      case (Some(b), a) => val r = " @" + b.toUpperCase; if (b == a.getOrElse("")) cyan(r) else r
      case (None, _) => ""
    }
  }
  private val renderTags = tags.toList.sorted.map(t => s" :$t").mkString
  private val renderStatus = status.fold("")(" ^" + _)
  private val indexed = List(ref, description, renderStatus, renderBy(None).toLowerCase, renderTags).mkString(" ")

  def search(query: String) = indexed.contains(query)

  def render(hideStatus: Boolean = false, hideBy: Boolean = false, hideTags: Boolean = false, highlight: Boolean = false, highlightAka: Option[String] = None) = {
    val r = s"$ref: $description${if (hideTags) "" else renderTags}${if (hideBy) "" else renderBy(highlightAka)}${if (hideStatus) "" else renderStatus}"
    if (highlight) orange(r) else r
  }
}

case class History(who: String, command: String)

case class Release(tag: String, issues: List[Issue])

case class IssueCreation(created: Issue, updatedModel: Model)

case class Tag(name: String, count: Int)

case class Model(workflowStates: List[String], userToAka: immutable.Map[String, String], issues: List[Issue], released: List[Release]) {
  def knows_?(who: String) = userToAka.contains(who)

  def createIssue(args: List[String], status: Option[String], by: Option[String], refProvider: RefProvider): Either[List[String], IssueCreation] = {
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
    val maybeDupe = issues.find(i => i.description == description)
    if (maybeDupe.isDefined) return Left(Messages.duplicateIssue(maybeDupe.get.ref))
    val newRef = refProvider.next
    val created = Issue(newRef, description, status, by, tagBits.toSet)
    val updatedModel = this.copy(issues = created :: this.issues)
    Right(IssueCreation(created, updatedModel))
  }

  def updateIssue(updated: Issue) = {
    val index = this.issues.indexOf(findIssue(updated.ref).get)
    this.copy(issues = this.issues.updated(index, updated))
  }

  def aka(who: String) = userToAka(who)
  def akas = userToAka.values.toList.distinct
  def findIssue(ref: String) = issues.find(_.ref == ref)
  def beginState = workflowStates.head
  def state(number: Int) = workflowStates(number) //TODO: this obviously needs thinking about if the states change
  def endState = workflowStates.reverse.head
  def releasableIssues = issues.filter(_.status == Some(endState))
  def releaseTags = released.map(_.tag)
  def allIssuesIncludingReleased = released.map(_.issues).flatten ++ issues

  def tags = {
    val allTheTags = allIssuesIncludingReleased.map(_.tags).flatten
    val uniqueTags = allTheTags.distinct
    uniqueTags.map(t => Tag(t, allTheTags.count(_ == t)))
  }
}

case class In(head: Option[String], tail:List[String])
case class Out(messages: List[String] = Nil, updatedModel: Option[Model] = None)








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





