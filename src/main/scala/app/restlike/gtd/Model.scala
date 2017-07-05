package app.restlike.gtd

import server.ServiceFactory.systemClock
import app.restlike.common.Colours._
import app.restlike.common._
import org.joda.time.{LocalDate, DateTime}

import scala.collection.immutable

case class Universe(userToModel: immutable.Map[String, Model], tokenToUser: immutable.Map[String, String]) {
  def modelFor(token: String) = if (tokenToUser.contains(token)) Some(userToModel(tokenToUser(token)))
  else None

  def updateModelFor(token: String, updatedModel: Model) =
    copy(userToModel = userToModel.updated(tokenToUser(token), updatedModel))
}

//TIP: useful chars - http://www.chriswrites.com/how-to-type-common-symbols-and-special-characters-in-os-x/
case class Thing(ref: String, description: String, date: Option[LocalDate], tags: Set[String] = Set.empty/*, history: Seq[History] = Seq.empty*/) {
//  private def renderBy(highlightAka: Option[String]) = {
//    (by, highlightAka) match {
//      case (Some(b), a) => val r = " @" + b.toUpperCase; if (b == a.getOrElse("")) customBlue(r) else cyan(r)
//      case (None, _) => ""
//    }
//  }

  //TODO: should this also support 'overdue' and 'really-overdue' .. then colours work purely off the strings
  //TODO: these should be constants to and sort by them .. zipwithindex etc
  def inferredState(model: Option[Model]) = {
    val today = systemClock().date
    date match {
      case Some(x) if model.fold(List.empty[Thing])(_.done).contains(this) => "done"
      case Some(d) if d.isBefore(systemClock().date.minusDays(1)) => "next-really-overdue"
      case Some(d) if d.isBefore(systemClock().date) => "next-overdue"
      case Some(x) if x == today || x.isBefore(today) => "next"
      case Some(x) => "deferred"
      case None => "collected"
    }
  }

  private val renderTags = customIvory(tags.toList.sorted.map(t => s" :$t").mkString)

  private def renderStatus(model: Option[Model]) = {
    val value = date.fold("")(s" ^${inferredState(model)} - " + _)
    ColouredForStatus(inferredState(model), value)
  }

  private val indexed = List(ref, description, renderStatus(None), renderTags).mkString(" ")

  def search(query: String) = indexed.contains(query)

  def render(model: Model, hideStatus: Boolean = false, hideBy: Boolean = false, hideTags: Boolean = false, hideId: Boolean = false, highlight: Boolean = false, highlightAka: Option[String] = None) = {
    val theRef = s"$ref: "
    val r = s"${if (hideId) "" else ColouredForStatus(inferredState(Some(model)) , "►︎ ")}${if (hideId) "" else if (highlight) customGreen(theRef) else customGrey(theRef)}${if (highlight) customGreen(description) else customGrey(description)}${if (hideTags) "" else renderTags}${if (hideStatus) "" else renderStatus(Some(model)) }"
    if (highlight) customGreen(r) else customGrey(r)
    r
  }
}

object ColouredForStatus {
  def apply(state: String, value: String) =
    state match {
      case "collected" => customBlue(value)
      case "done" => customGreen(value)
      case "deferred" => customGrey(value)
      case "next-really-overdue" => customRed(value)
      case "next-overdue" => customOrange(value)
      case "next" => customYellow(value)
      //        case Some(x) if x == m.endStateIndex => customGreen(value) //customOrange(value)
      case _ => customGrey(value) //customYellow(value)
    }
}

//case class History(who: String, command: String)

case class Release(tag: String, issues: List[Thing], when: Option[DateTime])

case class IssueCreation(created: Thing, updatedModel: Model)

case class Tag(name: String, count: Int)

case class Model(/*workflowStates: List[String],*/ /*userToAka: immutable.Map[String, String],*/ things: List[Thing], done: List[Thing], priorityTags: List[String]) {
//  def knows_?(who: String) = userToAka.contains(who)
//  def onBoard_?(issue: Thing) = issue.date.fold(false)(workflowStates.contains(_))

//  def collectedNeedProcessing = things.filter(_.date.isEmpty).nonEmpty

  def createIssue(args: List[String], date: Option[LocalDate], by: Option[String], refProvider: RefProvider): Either[List[String], IssueCreation] = {
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
    val maybeDupe = things.find(i => i.description == description)
    if (maybeDupe.isDefined) return Left(Messages.duplicateIssue(maybeDupe.get.ref))
    val newRef = refProvider.next
    val created = Thing(newRef, description, date, tagBits.toSet)
    val updatedModel = this.copy(things = created :: this.things)
    Right(IssueCreation(created, updatedModel))
  }

  def updateIssue(updated: Thing) = {
    val index = this.things.indexOf(findIssue(updated.ref).get)
    this.copy(things = this.things.updated(index, updated))
  }

//  def aka(who: String) = userToAka(who)
//  def akas = userToAka.values.toList.distinct
  def findIssue(ref: String) = things.find(_.ref == ref)
  def findDone(ref: String) = done.find(_.ref == ref)
//  def beginState = workflowStates.head
//  def state(number: Int) = workflowStates(number) //TODO: this obviously needs thinking about if the states change
//  def endStateIndex = workflowStates.reverse.head
//  def releasableIssues = things.filter(_.date == Some(endStateIndex))
//  def releaseTags = done.map(_.tag)
  def allThingsIncludingDone = done ++ things

//  def tags = {
//    val allTheTags = allIssuesIncludingReleased.map(_.tags).flatten
//    val uniqueTags = allTheTags.distinct
//    uniqueTags.map(t => Tag(t, allTheTags.count(_ == t)))
//  }
}

case class In(head: Option[String], tail: List[String])
case class Out(messages: Seq[String] = Nil, updatedModel: Option[Model] = None)
