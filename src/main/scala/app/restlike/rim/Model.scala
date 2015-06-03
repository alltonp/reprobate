package app.restlike.rim

import app.restlike.common.Colours._
import app.restlike.common._

import scala.collection.immutable

case class Issue(ref: String, description: String, status: Option[String], by: Option[String], tags: Set[String] = Set.empty/*, history: Seq[History] = Seq.empty*/) {
  private def renderBy(highlightAka: Option[String]) = {
    (by, highlightAka) match {
      case (Some(b), a) => val r = " @" + b.toUpperCase; if (b == a.getOrElse("")) cyan(r) else r
      case (None, _) => ""
    }
  }
  private val renderTags = magenta(tags.toList.sorted.map(t => s" :$t").mkString)
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

case class In(head: Option[String], tail: List[String])
case class Out(messages: List[String] = Nil, updatedModel: Option[Model] = None)
