package app.restlike.rim

import app.ServiceFactory
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
//TODO: 'by' should really be something else - like what?
//TODO: status should be an Int and workflowStatuses should include the first and last stastes (maybe)
//TODO: later we probably want who created it
//TODO: replace tds with when: Option - for when things are moved out of the backlog
//TODO: try to lose the status 'released' .. should be None at this point
//TODO: ultimately first and last states could be moved out to seperate persistence

//TODO: so we kind of want status to be None when released
//TODO: so maybe Some(-1) when in backlog, but that would look pants in the jsons ..
//TODO: so we actually want None on either .. hmmm ... how will rim ? work

//TODO: I need and updated: Option[Long] ... find easy way to update everywhere
//TODO: maybe add creator - but where would we show it?
case class Issue(ref: String, name: String, added: Long, status: Option[String], by: Option[String], blocked: Option[String], tags: Set[String] = Set.empty /*, history: Seq[History] = Seq.empty*/) {
  private def renderBy(highlightAka: Option[String]) = {
    (by, highlightAka) match {
      case (Some(b), a) => val r = " @" + b.toUpperCase; if (b == a.getOrElse("")) customBlue(r) else cyan(r)
      case (None, _) => ""
    }
  }

  private val renderTags = customIvory(tags.toList.sorted.map(t => s" :$t").mkString)
  private val renderBlocked = customRed(blocked.getOrElse(""))

  private def renderStatus(model: Option[Model]) = {
    val value = status.fold("")(" ^" + _)
    colouredForStatus(model, value)
  }

  private def colouredForStatus(model: Option[Model], value: String) = {
    model.fold(value)(m =>
      status match {
        case None => customGrey(value)
        case Some(x) if blocked.isDefined => customRed(value)
        case Some(x) if x == m.beginState => customYellow(value)
        case Some(x) if x == m.endState => customGreen(value)
        case Some(m.config.`postWorkflowState`) => customMagenta(value)
        case _ => customOrange(value)
      })
  }

  private val indexed = List(ref, name, renderStatus(None), renderBy(None).toLowerCase, renderBlocked, renderTags).mkString(" ")

  def search(query: String) = indexed.contains(query)

  def render(model: Model, hideStatus: Boolean = false, hideBy: Boolean = false, hideTags: Boolean = false, hideId: Boolean = false, highlight: Boolean = false, highlightAka: Option[String] = None) = {
    val theRef = s"$ref: "
    s"${if (hideId) "" else colouredForStatus(Some(model), "◼︎ ")}${if (hideId) "" else if (highlight) customGreen(theRef) else customGrey(theRef)}${if (highlight) customGreen(name) else customGrey(name)}${if (hideTags) "" else renderTags}${if (hideBy) "" else renderBy(highlightAka)}${if (hideStatus) "" else renderStatus(Some(model))} $renderBlocked"
  }
}

//TODO: when shoould be a timestamp
//TODO: issues should no longer need a status .. avoid "released" everwhere
case class Release(tag: String, issues: List[Issue], when: Option[DateTime])

case class IssueCreation(created: Issue, updatedModel: Model)

case class Tag(name: String, count: Int)

//TODO: thoughts about auth etc
//so 1 token per instance (outside json)
//list of writers email (can be inside json)
//read only token (can be inside json) - put share link in ui
//or email to list of tokens have access to
//somewhere else list of users to encrypted passwords/aka

//(1) web users auth and use their email to get list of tokens
//(2) cli users know token, can just look up
//(3) to lock out all change token
//(4) to lock out webuser remove email
//(5) borrow the bcrypt from thing
//(6) be able to create new from ui/url create/name/email - sends token to email

case class Config(name: String, preWorkflowState: String, workflowStates: List[String], postWorkflowState: String, priorityTags: List[String])

case class Model(config: Config, userToAka: immutable.Map[String, String], issues: List[Issue], released: List[Release]) {
  def knows_?(who: String) = userToAka.contains(who)
  def onBoard_?(issue: Issue) = issue.status.fold(false)(config.workflowStates.contains(_))

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
    val maybeDupe = issues.find(i => i.name == description)
    if (maybeDupe.isDefined) return Left(Messages.duplicateIssue(maybeDupe.get.ref))
    val newRef = refProvider.next
    val created = Issue(newRef, description, ServiceFactory.systemClock().dateTime.getMillis, status, by, None, tagBits.toSet)
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
  def beginState = config.workflowStates.head
  def state(number: Int) = config.workflowStates(number) //TODO: this obviously needs thinking about if the states change
  def endState = config.workflowStates.reverse.head
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
case class Out(messages: Seq[String] = Nil, updatedModel: Option[Model] = None, changed: Seq[String])
