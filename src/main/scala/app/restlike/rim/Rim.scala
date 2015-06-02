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

object RimCommander {
  def process(value: String, who: String, currentModel: Model, refProvider: RefProvider): Out = {
    val bits = value.split(" ").map(_.trim).filterNot(_.isEmpty)
    val cmd = In(bits.headOption, if (bits.isEmpty) Nil else bits.tail.toList)

    if (!cmd.head.getOrElse("").equals("aka") && !currentModel.knows_?(who)) return Out(Messages.notAuthorised(who), None)

    def aka = currentModel.aka(who)

    //TODO: be nice of the help could be driven off this ...
    cmd match {
      case In(None, Nil) => onShowBoard(currentModel, aka)
      case In(Some("aka"), List(myAka)) => onAka(who, myAka, currentModel)
      case In(Some("help"), Nil) => onHelp(currentModel, aka)
      case In(Some("+"), args) => onAddIssue(args, currentModel, refProvider)
      case In(Some("+/"), args) => onAddAndBeginIssue(args, currentModel, refProvider, aka)
      case In(Some("+//"), args) => onAddAndForwardIssue(args, currentModel, refProvider, aka)
      case In(Some("+!"), args) => onAddAndEndIssue(args, currentModel, refProvider, aka)
      case In(Some("?"), Nil) => onQueryIssues(currentModel, Nil, aka)
      case In(Some("?"), terms) => onQueryIssues(currentModel, terms, aka)
      case In(Some("."), Nil) => onShowBacklog(currentModel, aka)
      case In(Some(ref), List("-")) => onRemoveIssue(ref, currentModel)
      case In(Some(ref), args) if args.nonEmpty && args.head == "=" => onEditIssue(ref, args.drop(1), currentModel)
      case In(Some(ref), List("/")) => onForwardIssue(ref, currentModel, aka)
      case In(Some(ref), List("/!")) => onFastForwardIssue(ref, currentModel, aka)
      case In(Some(ref), List(".")) => onBackwardIssue(ref, currentModel, aka)
      case In(Some(ref), List(".!")) => onFastBackwardIssue(ref, currentModel, aka)
      case In(Some(ref), List("@")) => onOwnIssue(who, ref, currentModel)
      case In(Some(ref), List("@-")) => onDisownIssue(who, ref, currentModel)
      case In(Some(ref), args) if args.size == 2 && args.head == "@=" => onAssignIssue(args.drop(1).head.toUpperCase, ref, currentModel)
      case In(Some("@"), Nil) => onShowWhoIsDoingWhat(currentModel)
      case In(Some(ref), args) if args.nonEmpty && args.size > 1 && args.head == ":" => onTagIssue(ref, args.drop(1), currentModel)
      case In(Some(ref), args) if args.nonEmpty && args.size > 1 && args.head == ":-" => onDetagIssue(ref, args.drop(1), currentModel)
      case In(Some(oldTag), args) if args.nonEmpty && args.size == 2 && args.head == ":=" => onMigrateTag(oldTag, args.drop(1).head, currentModel)
      case In(Some(":"), Nil) => onShowTags(currentModel)
      case In(Some(":"), args) if args.nonEmpty && args.size == 1 => onShowAllForTag(args.head, currentModel)
      case In(Some("release"), List(tag)) => onRelease(tag, currentModel)
      case In(Some("releases"), Nil) => onShowReleases(currentModel)
      case In(Some("note"), args) if args.nonEmpty && args.size == 1 => onShowReleaseNote(args.head, currentModel)
      case In(head, tail) => onUnknownCommand(head, tail)
    }
  }

  private def onUnknownCommand(head: Option[String], tail: List[String]) =
    Out(red(Messages.eh) + " " + head.getOrElse("") + " " + tail.mkString(" ") :: Nil, None)

  private def onShowBoard(currentModel: Model, aka: String) = Out(Presentation.board(currentModel, Nil, aka), None)

  private def onHelp(currentModel: Model, aka: String) = Out(Messages.help(aka), None)

  private def onShowReleases(currentModel: Model) = {
    val all = currentModel.released.flatMap(Presentation.release(_))
    val result = if (all.isEmpty) Messages.success(s"no releases found")
    else all
    Out(result, None)
  }

  private def onShowWhoIsDoingWhat(currentModel: Model) = {
    val akas = currentModel.akas
    val all = akas.map(aka => {
      val issues = currentModel.issues.filter(_.by == Some(aka))
      Presentation.issuesForUser(aka, issues)
    })

    val result = if (all.isEmpty) Messages.success(s"nobody is doing anything")
    else all
    Out(result, None)
  }

  private def onShowTags(currentModel: Model) = {
    val all = currentModel.tags
    val result = if (all.isEmpty) Messages.success(s"no tags found")
    else Presentation.tags(all)
    Out(result, None)
  }

  private def onShowAllForTag(tag: String, currentModel: Model) = {
    val issuesWithTag = currentModel.allIssuesIncludingReleased.filter(_.tags.contains(tag))
    val result = if (issuesWithTag.isEmpty) Messages.success(s"no issues found for tag: $tag")
    else Presentation.tagDetail(tag, issuesWithTag, currentModel)
    Out(result, None)
  }

  private def onShowReleaseNote(release: String, currentModel: Model) = {
    val maybeRelease = currentModel.released.find(_.tag == release)
    val result = if (maybeRelease.isEmpty) Messages.problem(s"no release found for: $release")
    else Presentation.releaseNotes(release, maybeRelease.get.issues, currentModel).toList
    Out(result, None)
  }

  private def onRelease(tag: String, currentModel: Model): Out = {
    val releaseable = currentModel.releasableIssues
    val remainder = currentModel.issues diff releaseable

    if (currentModel.releaseTags.contains(tag)) return Out(Messages.problem(s"$tag has already been released"), None)
    if (releaseable.isEmpty) return Out(Messages.problem(s"nothing to release for $tag"), None)

    val release = Release(tag, releaseable)
    val updatedModel = currentModel.copy(issues = remainder, released = release :: currentModel.released )

    Out(Presentation.release(release), Some(updatedModel))
  }

  private def onMigrateTag(oldTag: String, newTag: String, currentModel: Model) = {
    def migrateTags(tags: Set[String]): Set[String] = tags - oldTag + newTag
    def migrateIssue(i: Issue): Issue = i.copy(tags = if (i.tags.contains(oldTag)) migrateTags(i.tags) else i.tags)

    if (currentModel.tags.map(_.name).contains(oldTag)) {
      val updatedModel = currentModel.copy(
        issues = currentModel.issues.map(i => {
          migrateIssue(i)
        }),
        released = currentModel.released.map(r => {
          r.copy(issues = r.issues.map(i => migrateIssue(i)))
        })
      )
      //TODO: should show the issues that have changed as a result
      Out(Presentation.tags(updatedModel.tags), Some(updatedModel))
    } else Out(Messages.problem(s"$oldTag does not exist"))
  }

  private def onDetagIssue(ref: String, args: List[String], currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newTags = found.tags -- args
      val updatedIssue = found.copy(tags = newTags)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Messages.successfulUpdate(s":- ${updatedIssue.render()}"), Some(updatedModel))
    }
  }

  private def onTagIssue(ref: String, args: List[String], currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newTags = found.tags ++ args
      val updatedIssue = found.copy(tags = newTags)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Messages.successfulUpdate(s": ${updatedIssue.render()}"), Some(updatedModel))
    }
  }

  private def onOwnIssue(who: String, ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val updatedIssue = found.copy(by = Some(currentModel.userToAka(who)))
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Messages.successfulUpdate(s"@ ${updatedIssue.render()}"), Some(updatedModel))
    }
  }

  private def onDisownIssue(who: String, ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val updatedIssue = found.copy(by = None)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Messages.successfulUpdate(s"@ ${updatedIssue.render()}"), Some(updatedModel))
    }
  }

  private def onAssignIssue(assignee: String, ref: String, currentModel: Model): Out = {
    if (!currentModel.userToAka.values.toSeq.contains(assignee)) return Out(Messages.problem(s"$assignee is not one of: ${currentModel.userToAka.values.mkString(", ")}"))
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val updatedIssue = found.copy(by = Some(assignee))
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Messages.successfulUpdate(s"@ ${updatedIssue.render()}"), Some(updatedModel))
    }
  }

  //TODO: model.forwardAState
  //TODO: model.backwardAState
  private def onBackwardIssue(ref: String, currentModel: Model, aka: String) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newStatus = if (found.status.isEmpty) None
      else {
        val currentIndex = currentModel.workflowStates.indexOf(found.status.get)
        if (currentIndex <= 0) None else Some(currentModel.workflowStates(currentIndex - 1))
      }
      val by = if (newStatus.isEmpty || newStatus == Some(currentModel.beginState)) None else Some(aka)
      val updatedIssue = found.copy(status = newStatus, by = by)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.board(updatedModel, Seq(ref), aka), Some(updatedModel))
    }
  }

  private def onFastBackwardIssue(ref: String, currentModel: Model, aka: String) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newStatus = None
      val updatedIssue = found.copy(status = newStatus, by = None)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.board(updatedModel, Seq(ref), aka), Some(updatedModel))
    }
  }

  private def onForwardIssue(ref: String, currentModel: Model, aka: String) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newStatus = if (found.status.isEmpty) currentModel.beginState
      else {
        val currentIndex = currentModel.workflowStates.indexOf(found.status.get)
        val newIndex = if (currentIndex >= currentModel.workflowStates.size - 1) currentIndex else currentIndex + 1
        currentModel.workflowStates(newIndex)
      }
      val by = if (newStatus == currentModel.beginState) None else Some(aka)
      val updatedIssue = found.copy(status = Some(newStatus), by = by)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.board(updatedModel, Seq(ref), aka), Some(updatedModel))
    }
  }

  private def onFastForwardIssue(ref: String, currentModel: Model, aka: String) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newStatus = currentModel.endState
      val updatedIssue = found.copy(status = Some(newStatus), by = Some(aka))
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.board(updatedModel, Seq(ref), aka), Some(updatedModel))
    }
  }

  private def onEditIssue(ref: String, args: List[String], currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newDescription = args.mkString(" ")
      val updatedIssue = found.copy(description = newDescription)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Messages.successfulUpdate(s"= ${updatedIssue.render()}"), Some(updatedModel))
    }
  }

  private def onRemoveIssue(ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val updatedModel = currentModel.copy(issues = currentModel.issues.filterNot(i => i == found))
      Out(Messages.successfulUpdate(s"- ${found.render()}"), Some(updatedModel))
    }
  }

  //TODO: add search to Model
  private def onQueryIssues(currentModel: Model, terms: List[String], aka: String) = {
    def query(issues: List[Issue], terms: List[String]): List[Issue] = {
      terms match {
        case Nil => issues
        case(ts) => query(issues.filter(i => i.search(ts.head)), ts.tail)
      }
    }

    val matching = query(currentModel.allIssuesIncludingReleased, terms)
    val result = if (matching.isEmpty) (s"no issues found" + (if (terms.nonEmpty) s" for: ${terms.mkString(" ")}" else "")) :: Nil
    else matching.sortBy(_.ref.toInt).reverseMap(i => i.render(highlightAka = Some(aka)))
    Out(result, None)
  }

  private def onShowBacklog(currentModel: Model, aka: String) = {
    val matching = currentModel.issues.filter(i => i.status.isEmpty)
    val result = if (matching.isEmpty) s"backlog is empty" :: Nil
    else matching.reverseMap(i => i.render(highlightAka = Some(aka)))
    Out(result, None)
  }

  private def onAddIssue(args: List[String], currentModel: Model, refProvider: RefProvider) = {
    currentModel.createIssue(args, None, None, refProvider) match {
      case Left(e) => Out(e, None)
      case Right(r) => Out(Messages.successfulUpdate(s"+ ${r.created.render()}"), Some(r.updatedModel))
    }
  }

  private def onAddAndBeginIssue(args: List[String], currentModel: Model, refProvider: RefProvider, aka: String) = {
    currentModel.createIssue(args, Some(currentModel.beginState), None, refProvider) match {
      case Left(e) => Out(e, None)
      case Right(r) => Out(Presentation.board(r.updatedModel, Seq(r.created.ref), aka), Some(r.updatedModel))
    }
  }

  private def onAddAndForwardIssue(args: List[String], currentModel: Model, refProvider: RefProvider, aka: String) = {
    currentModel.createIssue(args, Some(currentModel.state(1)), Some(aka), refProvider) match {
      case Left(e) => Out(e, None)
      case Right(r) => Out(Presentation.board(r.updatedModel, Seq(r.created.ref), aka), Some(r.updatedModel))
    }
  }

  private def onAddAndEndIssue(args: List[String], currentModel: Model, refProvider: RefProvider, aka: String) = {
    currentModel.createIssue(args, Some(currentModel.endState), Some(aka), refProvider) match {
      case Left(e) => Out(e, None)
      case Right(r) => Out(Presentation.board(r.updatedModel, Seq(r.created.ref), aka), Some(r.updatedModel))
    }
  }

  private def onAka(who: String, aka: String, currentModel: Model): Out = {
    if (aka.size > 3) return Out(Messages.problem("maximum 3 chars"), None)
    val updatedModel = currentModel.copy(userToAka = currentModel.userToAka.updated(who, aka.toUpperCase))
    Out(Messages.help(aka.toUpperCase), Some(updatedModel))
  }
}

//TODO: add issue
object Presentation {
  def board(model: Model, changed: Seq[String], aka: String) = {
    groupByStatus(model.issues, model, changed, Some(aka))
  }
  
  def release(release: Release) = {
    val r = release.issues.map(i => s"\n  ${i.render(hideStatus = true)}").mkString
    s"${release.tag}: (${release.issues.size})" + r + "\n" :: Nil
  }

  def issuesForUser(aka: String, issues: List[Issue]) = {
    val r = issues.map(i => s"\n  ${i.render(hideBy = true)}").mkString
    s"${aka}: (${issues.size})" + r + "\n"
  }

  def tags(all: Seq[Tag]) =
    ": " + sortedByPopularity(all).map(t => s"${t.name} (${t.count})").mkString(", ") :: Nil

  //TODO: we should include the released on the board too
  //TODO: render or remove tag
  def tagDetail(tag: String, issues: Seq[Issue], currentModel: Model) = {
    groupByStatus(issues, currentModel, Nil, None)
  }

  //TODO: render or remove release
  def releaseNotes(release: String, issues: Seq[Issue], currentModel: Model) = {
    val tagNames = issues.flatMap(_.tags).distinct
    println(tagNames)
    val tags = currentModel.tags.filter(t => tagNames.contains(t.name))
    sieveByTag(sortedByPopularity(tags), issues, currentModel)
  }

  private def groupByStatus(issues: Seq[Issue], currentModel: Model, changed: Seq[String], aka: Option[String]) = {
    val stateToIssues = issues.groupBy(_.status)
    currentModel.workflowStates.map(s => {
      val issuesForState = stateToIssues.getOrElse(Some(s), Nil)
      val issues = issuesForState.map(i => s"\n  ${
        i.render(hideStatus = true, hideBy = true, hideTags = true, highlight = changed.contains(i.ref), highlightAka = aka)
      }").mkString
      s"$s: (${issuesForState.size})" + issues + "\n"
    })
  }

  private def sieveByTag(tags: Seq[Tag], issues: Seq[Issue], currentModel: Model) = {
    case class TagAndIssues(tag: String, issues: Seq[Issue])
//    println(tags.mkString(", "))
    var remainingIssues = issues
    val r = tags.map(t => {
      val issuesForTag = remainingIssues.filter(_.tags.contains(t.name))
//      println(s"\n$t: $issuesForTag")
      remainingIssues = remainingIssues.diff(issuesForTag)
//      renderTagAndIssues(t.name, issuesForTag)
      TagAndIssues(t.name, issuesForTag)
    }) ++ Seq(TagAndIssues("?", remainingIssues))
    r.filterNot(_.issues.isEmpty).sortBy(_.issues.size).reverseMap(tai => renderTagAndIssues(tai.tag, tai.issues))
  }

  private def renderTagAndIssues(tag: String, issuesForTag: Seq[Issue]): String = {
    val issues = issuesForTag.map(i => s"\n  ${
      i.render(hideStatus = true, hideBy = true)
    }").mkString
    s"$tag: (${issuesForTag.size})" + issues + "\n"
  }

  private def sortedByPopularity(all: Seq[Tag]) = all.sortBy(t => (-t.count, t.name))
}



object Persistence {
  private val file = Paths.get("rim.json")
  private val defaultStatuses = List("next", "doing", "done")

  def load: Model = {
    if (!file.toFile.exists()) save(Model(defaultStatuses, immutable.Map[String, String](), List[Issue](), List[Release]()))
    Json.deserialise(Filepath.load(file))
  }

  def save(state: Model) {
    Filepath.save(pretty(render(Json.serialise(state))), file)
  }
}

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

object Rim extends RestHelper {
  import app.restlike.rim.Messages._
  import Responder._

  //TODO: extract app name
  serve {
    case r@Req("rim" :: "install" :: Nil, _, GetRequest) ⇒ () ⇒ t(Script.install("rim"), downcase = false)
    case r@Req("rim" :: "tracking" :: Nil, _, GetRequest) ⇒ () ⇒ t(Tracker("rim.tracking").view, downcase = false)
    case r@Req("rim" :: "state" :: Nil, _, GetRequest) ⇒ () ⇒ Full(JsonResponse(Json.serialise(Persistence.load)))
    case r@Req("rim" :: who :: Nil, _, PostRequest) ⇒ () ⇒ Controller.process(who, r)
  }
}



