package app.restlike.rem

import app.restlike.common.Colours._
import app.restlike.common._

import scala.collection.immutable

case class Thing(ref: String, key: String, value: Option[String], tags: Set[String] = Set.empty/*, history: Seq[History] = Seq.empty*/) {
  val description = s"$key = $value"
//  description: String, status: Option[String], by: Option[String]
//  private val renderBy = by.fold("")(" @" + _)
  private val renderTags = tags.toList.sorted.map(t => s" :$t").mkString
//  private val renderStatus = status.fold("")(" ^" + _)
  private val indexed = List(ref/*, description, renderStatus, renderBy.toLowerCase,*/, key, value.getOrElse(""), renderTags).mkString(" ")

  def search(query: String) = indexed.contains(query)
  def render() = s"${lightGrey(s"$ref:")} ${customYellow(key)}${value.fold("")(v => s" ${lightGrey("=")} ${cyan(v)}")}${lightGrey(renderTags)}"
}

//case class History(who: String, command: String)

//case class Release(tag: String, issues: List[Thing])

case class IssueCreation(created: Thing, updatedModel: Model)

case class Tag(name: String, count: Int)

case class Universe(userToModel: immutable.Map[String, Model], tokenToUser: immutable.Map[String, String]) {
  def modelFor(token: String) = if (tokenToUser.contains(token)) Some(userToModel(tokenToUser(token)))
                                else None

  def updateModelFor(token: String, updatedModel: Model) =
    copy(userToModel = userToModel.updated(tokenToUser(token), updatedModel))
}

case class Model(/*userToAka: immutable.Map[String, String],*/ things: List[Thing]) {
//  def knows_?(who: String) = userToAka.contains(who)

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
    if (maybeDupe.isDefined) return Left(Messages.duplicateThing(maybeDupe.get.ref))
    val newRef = refProvider.next
    val created = Thing(newRef, keyValueBits.head, keyValueBits.drop(1).headOption, tagBits.toSet)
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

case class In(head: Option[String], tail: List[String])
case class Out(messages: List[String] = Nil, updatedModel: Option[Model] = None)
