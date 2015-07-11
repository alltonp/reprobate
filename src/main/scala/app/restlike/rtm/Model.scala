package app.restlike.rtm

import app.restlike.common.Colours._
import app.restlike.common._
import org.joda.time.DateTime

import scala.collection.immutable


case class Universe(userToModel: immutable.Map[String, Model], tokenToUser: immutable.Map[String, String]) {
  def modelFor(token: String) = if (tokenToUser.contains(token)) Some(userToModel(tokenToUser(token)))
  else None

  def updateModelFor(token: String, updatedModel: Model) =
    copy(userToModel = userToModel.updated(tokenToUser(token), updatedModel))
}

//TIP: useful chars - http://www.chriswrites.com/how-to-type-common-symbols-and-special-characters-in-os-x/
case class Thing(ref: String, description: String, status: Option[String], tags: Set[String] = Set.empty/*, history: Seq[History] = Seq.empty*/) {
//  private def renderBy(highlightAka: Option[String]) = {
//    (by, highlightAka) match {
//      case (Some(b), a) => val r = " @" + b.toUpperCase; if (b == a.getOrElse("")) customBlue(r) else cyan(r)
//      case (None, _) => ""
//    }
//  }

  private val renderTags = customIvory(tags.toList.sorted.map(t => s" :$t").mkString)

  private def renderStatus(model: Option[Model]) = {
    val value = status.fold("")(" ^" + _)
    colouredForStatus(model, value)
  }

  private def colouredForStatus(model: Option[Model], value: String) = {
    model.fold(value)(m =>
      status match {
        case None => customGrey(value)
        case Some(x) if x == m.beginState => customYellow(value) //cyan(value)
        case Some(x) if x == m.endState => customGreen(value) //customOrange(value)
        case Some("released") => customMagenta(value)
        case _ => customOrange(value) //customYellow(value)
      })
  }

  private val indexed = List(ref, description, renderStatus(None), renderTags).mkString(" ")

  def search(query: String) = indexed.contains(query)

  def render(model: Model, hideStatus: Boolean = false, hideBy: Boolean = false, hideTags: Boolean = false, hideId: Boolean = false, highlight: Boolean = false, highlightAka: Option[String] = None) = {
    val theRef = s"$ref: "
    val r = s"${if (hideId) "" else colouredForStatus(Some(model), "◼︎ ")}${if (hideId) "" else if (highlight) customGreen(theRef) else customGrey(theRef)}${if (highlight) customGreen(description) else customGrey(description)}${if (hideTags) "" else renderTags}${if (hideStatus) "" else renderStatus(Some(model))}"
//    if (highlight) customGreen(r) else customGrey(r)
    r
  }
}

//case class History(who: String, command: String)

case class Release(tag: String, issues: List[Thing], when: Option[DateTime])

case class IssueCreation(created: Thing, updatedModel: Model)

case class Tag(name: String, count: Int)

case class Model(workflowStates: List[String], /*userToAka: immutable.Map[String, String],*/ things: List[Thing], released: List[Release], priorityTags: List[String]) {
//  def knows_?(who: String) = userToAka.contains(who)
  def onBoard_?(issue: Thing) = issue.status.fold(false)(workflowStates.contains(_))

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
    val maybeDupe = things.find(i => i.description == description)
    if (maybeDupe.isDefined) return Left(Messages.duplicateIssue(maybeDupe.get.ref))
    val newRef = refProvider.next
    val created = Thing(newRef, description, status, tagBits.toSet)
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
  def beginState = workflowStates.head
  def state(number: Int) = workflowStates(number) //TODO: this obviously needs thinking about if the states change
  def endState = workflowStates.reverse.head
  def releasableIssues = things.filter(_.status == Some(endState))
  def releaseTags = released.map(_.tag)
  def allIssuesIncludingReleased = released.map(_.issues).flatten ++ things

  def tags = {
    val allTheTags = allIssuesIncludingReleased.map(_.tags).flatten
    val uniqueTags = allTheTags.distinct
    uniqueTags.map(t => Tag(t, allTheTags.count(_ == t)))
  }
}

case class In(head: Option[String], tail: List[String])
case class Out(messages: Seq[String] = Nil, updatedModel: Option[Model] = None)
