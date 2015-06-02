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

object Messages {
  val eh = "eh?"

  def notAuthorised(who: String) = List(red(s"easy ${who}, please set your initials first: ") + "'rem aka pa'")
  def notFound(ref: String) = problem(s"issue not found: $ref")
  def descriptionEmpty = problem(s"description is empty")
  def duplicateIssue(ref: String) = problem(s"issue already exists: $ref")
  def problem(message: String) = List(red(s"problem: ") + message)

  //TODO: how about advance and retreat instead of forward/back or push/pull or left/right
  def help(who: String) = List(
    s"hello ${who}, welcome to rem - the thing rememberer © 2015 spabloshi ltd",
    "",
    "things:",
    "  - create                         ⇒ 'rem + [key] = {value} {: tag1 tag2 tagX}'",
//    "  - update                         ⇒ 'rim [ref] ='",
    "  - delete                         ⇒ 'rem [ref] -'",
//    "  - own                            ⇒ 'rim [ref] @'",
//    "  - disown                         ⇒ 'rim [ref] @-'",
//    "  - assign                         ⇒ 'rim [ref] @= [aka]'",
//    "  - tag                            ⇒ 'rim [ref] : [tag1] {tag2} {tagX}'",
//    "  - detag                          ⇒ 'rim [ref] :- [tag1] {tag2} {tagX}'",
//  //TODO: pull out to be under 'tags' section?
//    "  - migrate tag                    ⇒ 'rim [oldtag] := [newtag]'",
//    "  - move forward                   ⇒ 'rim [ref] /'",
////    "  - move forward many              ⇒ 'rim [ref] //'",
//    "  - move to end                    ⇒ 'rim [ref] /!'",
//    "  - move backward                  ⇒ 'rim [ref] .'",
//    //    "  - move backward many         ⇒ 'rim [ref] ..'",
//    "  - return to backlog              ⇒ 'rim [ref] .!'",
    "",
    "show:",
    "  - all                            ⇒ 'rem'",
//    "  - backlog                        ⇒ 'rim .'",
//    "  - releases                       ⇒ 'rim releases'",
//    "  - tags                           ⇒ 'rim :'",
//    "  - who is doing what              ⇒ 'rim @'",
    "  - help                           ⇒ 'rem help'",
    "",
    "search:",
    "  - all issues                     ⇒ 'rem ? {term1 term2 termX}'                      ⇒ e.g. 'rem ? :tag text'",
    "",
//    //TODO: this will ultimately be 'config' once we also have 'releases'
    "other:",
    "  - set aka                        ⇒ 'rem aka [initials]'",
//    "  - create release                 ⇒ 'rim release [label]'",
//    "",
//    "expert mode:",
//    "  - create, forward and tag        ⇒ 'rim +/ description {: tag1 tag2 tagX}'",
//    "  - create, forward many and tag   ⇒ 'rim +// description {: tag1 tag2 tagX}'",
//    "  - create, end and tag            ⇒ 'rim +! description {: tag1 tag2 tagX}'",
    "",
    "where: [arg] = mandatory, {arg} = optional",
    ""
  )

  //TODO: parameterise and pull out
  val install =
    (s"""#!/bin/bash
      |#INSTALLATION:
      |#- alias rem='{path to}/rem.sh'
      |#- that's it!
      |
      |REM_HOST="http://${java.net.InetAddress.getLocalHost.getHostName}:8473"
      |""" + """OPTIONS="--timeout=15 --no-proxy -qO-"
      |WHO=`id -u -n`
      |BASE="rem/$WHO"
      |REQUEST="$OPTIONS $REM_HOST/$BASE"
      |MESSAGE="${@:1}"
      |RESPONSE=`wget $REQUEST --post-data="{\"value\":\"${MESSAGE}\"}" --header=Content-Type:application/json`
      |echo
      |if [ $? -ne 0 ]; then
      |  echo "\nsorry, rem seems to be unavailable right now, please try again later\n\n"
      |else
      |  echo "$RESPONSE"
      |fi
      |echo
      |`wget -qO.rem.bak $REM_HOST/rem/state`
      |
    """).stripMargin.split("\n").toList
}

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

object RemCommander {
  def process(value: String, who: String, currentModel: Model, refProvider: RefProvider): Out = {
    val bits = value.split(" ").map(_.trim).filterNot(_.isEmpty)
    val cmd = In(bits.headOption, if (bits.isEmpty) Nil else bits.tail.toList)

    if (!cmd.head.getOrElse("").equals("aka") && !currentModel.knows_?(who)) return Out(Messages.notAuthorised(who), None)

    //TODO: be nice of the help could be driven off this ...
    cmd match {
      //TODO: should propbably show somehting more useful, like most popular etc
//      case In(None, Nil) => onShowBoard(currentModel)
      case In(None, Nil) => onQueryThings(currentModel, Nil)
      case In(Some("aka"), List(aka)) => onAka(who, aka, currentModel)
      case In(Some("help"), Nil) => onHelp(who, currentModel)
      case In(Some("+"), args) => onAddThing(args, currentModel, refProvider)
//      case In(Some("+/"), args) => onAddAndBeginIssue(who, args, currentModel, refProvider)
//      case In(Some("+//"), args) => onAddAndForwardIssue(who, args, currentModel, refProvider)
//      case In(Some("+!"), args) => onAddAndEndIssue(who, args, currentModel, refProvider)
      case In(Some("?"), Nil) => onQueryThings(currentModel, Nil)
      case In(Some("?"), terms) => onQueryThings(currentModel, terms)
//      case In(Some("."), Nil) => onShowBacklog(currentModel)
      case In(Some(ref), List("-")) => onRemoveIssue(ref, currentModel)
//      case In(Some(ref), args) if args.nonEmpty && args.head == "=" => onEditIssue(ref, args.drop(1), currentModel)
//      case In(Some(ref), List("/")) => onForwardIssue(who, ref, currentModel)
//      case In(Some(ref), List("/!")) => onFastForwardIssue(who, ref, currentModel)
//      case In(Some(ref), List(".")) => onBackwardIssue(who, ref, currentModel)
//      case In(Some(ref), List(".!")) => onFastBackwardIssue(who, ref, currentModel)
//      case In(Some(ref), List("@")) => onOwnIssue(who, ref, currentModel)
//      case In(Some(ref), List("@-")) => onDisownIssue(who, ref, currentModel)
//      case In(Some(ref), args) if args.size == 2 && args.head == "@=" => onAssignIssue(args.drop(1).head.toUpperCase, ref, currentModel)
//      case In(Some("@"), Nil) => onShowWhoIsDoingWhat(currentModel)
//      case In(Some(ref), args) if args.nonEmpty && args.size > 1 && args.head == ":" => onTagIssue(ref, args.drop(1), currentModel)
//      case In(Some(ref), args) if args.nonEmpty && args.size > 1 && args.head == ":-" => onDetagIssue(ref, args.drop(1), currentModel)
//      case In(Some(oldTag), args) if args.nonEmpty && args.size == 2 && args.head == ":=" => onMigrateTag(oldTag, args.drop(1).head, currentModel)
//      case In(Some(":"), Nil) => onShowTags(currentModel)
//      case In(Some("release"), List(tag)) => onRelease(tag, currentModel)
//      case In(Some("releases"), Nil) => onShowReleases(currentModel)
      case In(head, tail) => onUnknownCommand(head, tail)
    }
  }

  private def onUnknownCommand(head: Option[String], tail: List[String]) =
    Out(red(Messages.eh) + " " + head.getOrElse("") + " " + tail.mkString(" ") :: Nil, None)

//  private def onShowBoard(currentModel: Model) = Out(Presentation.board(currentModel), None)

  private def onHelp(who: String, currentModel: Model) = Out(Messages.help(currentModel.aka(who)), None)

//  private def onShowReleases(currentModel: Model) = {
//    val all = currentModel.released.map(Presentation.release(_))
//    val result = if (all.isEmpty) s"no releases found" :: Nil
//    else all
//    Out(result, None)
//  }

//  private def onShowWhoIsDoingWhat(currentModel: Model) = {
//    val akas = currentModel.akas
//    val all = akas.map(aka => {
//      val issues = currentModel.things.filter(_.by == Some(aka))
//      Presentation.issuesForUser(aka, issues)
//    })
//
//    val result = if (all.isEmpty) s"nobody is doing anything" :: Nil
//    else all
//    Out(result, None)
//  }

  private def onShowTags(currentModel: Model) = {
    val all = currentModel.tags
    val result = if (all.isEmpty) s"no tags found" :: Nil
    else Presentation.tags(all)
    Out(result, None)
  }

//  private def onRelease(tag: String, currentModel: Model): Out = {
//    val releaseable = currentModel.releasableIssues
//    val remainder = currentModel.things diff releaseable
//
//    if (currentModel.releaseTags.contains(tag)) return Out(Messages.problem(s"$tag has already been released"), None)
//    if (releaseable.isEmpty) return Out(Messages.problem(s"nothing to release for $tag"), None)
//
//    val release = Release(tag, releaseable)
//    val updatedModel = currentModel.copy(things = remainder, released = release :: currentModel.released )
//
//    Out(Presentation.release(release) :: Nil, Some(updatedModel))
//  }

//  private def onMigrateTag(oldTag: String, newTag: String, currentModel: Model) = {
//    def migrateTags(tags: Set[String]): Set[String] = tags - oldTag + newTag
//    def migrateIssue(i: Thing): Thing = i.copy(tags = if (i.tags.contains(oldTag)) migrateTags(i.tags) else i.tags)
//
//    if (currentModel.tags.map(_.name).contains(oldTag)) {
//      val updatedModel = currentModel.copy(
//        things = currentModel.things.map(i => {
//          migrateIssue(i)
//        }),
//        released = currentModel.released.map(r => {
//          r.copy(issues = r.issues.map(i => migrateIssue(i)))
//        })
//      )
//      Out(Presentation.tags(updatedModel.tags), Some(updatedModel))
//    } else Out(Messages.problem(s"$oldTag does not exist"))
//  }

  private def onDetagIssue(ref: String, args: List[String], currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newTags = found.tags -- args
      val updatedIssue = found.copy(tags = newTags)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(s":- ${updatedIssue.render()}" :: Nil, Some(updatedModel))
    }
  }

  private def onTagIssue(ref: String, args: List[String], currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newTags = found.tags ++ args
      val updatedIssue = found.copy(tags = newTags)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(s": ${updatedIssue.render()}" :: Nil, Some(updatedModel))
    }
  }

//  private def onOwnIssue(who: String, ref: String, currentModel: Model) = {
//    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
//      val updatedIssue = found.copy(by = Some(currentModel.userToAka(who)))
//      val updatedModel = currentModel.updateIssue(updatedIssue)
//      Out(s"@ ${updatedIssue.render()}" :: Nil, Some(updatedModel))
//    }
//  }

//  private def onDisownIssue(who: String, ref: String, currentModel: Model) = {
//    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
//      val updatedIssue = found.copy(by = None)
//      val updatedModel = currentModel.updateIssue(updatedIssue)
//      Out(s"@ ${updatedIssue.render()}" :: Nil, Some(updatedModel))
//    }
//  }

//  private def onAssignIssue(assignee: String, ref: String, currentModel: Model): Out = {
//    if (!currentModel.userToAka.values.toSeq.contains(assignee)) return Out(Messages.problem(s"$assignee is not one of: ${currentModel.userToAka.values.mkString(", ")}"))
//    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
//      val updatedIssue = found.copy(by = Some(assignee))
//      val updatedModel = currentModel.updateIssue(updatedIssue)
//      Out(s"@ ${updatedIssue.render()}" :: Nil, Some(updatedModel))
//    }
//  }

  //TODO: model.forwardAState
  //TODO: model.backwardAState
//  private def onBackwardIssue(who: String, ref: String, currentModel: Model) = {
//    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
//      val newStatus = if (found.status.isEmpty) None
//      else {
//        val currentIndex = currentModel.workflowStates.indexOf(found.status.get)
//        if (currentIndex <= 0) None else Some(currentModel.workflowStates(currentIndex - 1))
//      }
//      val by = if (newStatus.isEmpty || newStatus == Some(currentModel.beginState)) None else Some(currentModel.userToAka(who))
//      val updatedIssue = found.copy(status = newStatus, by = by)
//      val updatedModel = currentModel.updateIssue(updatedIssue)
//      Out(Presentation.board(updatedModel), Some(updatedModel))
//    }
//  }

//  private def onFastBackwardIssue(who: String, ref: String, currentModel: Model) = {
//    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
//      val newStatus = None
//      val updatedIssue = found.copy(status = newStatus, by = None)
//      val updatedModel = currentModel.updateIssue(updatedIssue)
//      Out(Presentation.board(updatedModel), Some(updatedModel))
//    }
//  }

//  private def onForwardIssue(who: String, ref: String, currentModel: Model) = {
//    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
//      val newStatus = if (found.status.isEmpty) currentModel.beginState
//      else {
//        val currentIndex = currentModel.workflowStates.indexOf(found.status.get)
//        val newIndex = if (currentIndex >= currentModel.workflowStates.size - 1) currentIndex else currentIndex + 1
//        currentModel.workflowStates(newIndex)
//      }
//      val by = if (newStatus == currentModel.beginState) None else Some(currentModel.userToAka(who))
//      val updatedIssue = found.copy(status = Some(newStatus), by = by)
//      val updatedModel = currentModel.updateIssue(updatedIssue)
//      Out(Presentation.board(updatedModel), Some(updatedModel))
//    }
//  }

//  private def onFastForwardIssue(who: String, ref: String, currentModel: Model) = {
//    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
//      val newStatus = currentModel.endState
//      val updatedIssue = found.copy(status = Some(newStatus), by = Some(currentModel.userToAka(who)))
//      val updatedModel = currentModel.updateIssue(updatedIssue)
//      Out(Presentation.board(updatedModel), Some(updatedModel))
//    }
//  }

//  private def onEditIssue(ref: String, args: List[String], currentModel: Model) = {
//    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
//      val newDescription = args.mkString(" ")
//      val updatedIssue = found.copy(description = newDescription)
//      val updatedModel = currentModel.updateIssue(updatedIssue)
//      Out(s"= ${updatedIssue.render()}" :: Nil, Some(updatedModel))
//    }
//  }

  private def onRemoveIssue(ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val updatedModel = currentModel.copy(things = currentModel.things.filterNot(i => i == found))
      Out(s"- ${found.render()}" :: Nil, Some(updatedModel))
    }
  }

  //TODO: add search to Model
  private def onQueryThings(currentModel: Model, terms: List[String]) = {
    def query(issues: List[Thing], terms: List[String]): List[Thing] = {
      terms match {
        case Nil => issues
        case(ts) => query(issues.filter(i => i.search(ts.head)), ts.tail)
      }
    }

    //TODO: add allIssues to model and tidy
    val allIssues = currentModel.things// ::: currentModel.released.flatMap(_.issues)
    val matching = query(allIssues, terms)
    val result = if (matching.isEmpty) (s"no things found" + (if (terms.nonEmpty) s" for: ${terms.mkString(" ")}" else "")) :: Nil
    else matching.sortBy(_.ref.toInt).reverseMap(i => i.render())
    Out(result, None)
  }

//  private def onShowBacklog(currentModel: Model) = {
//    val matching = currentModel.things.filter(i => i.status.isEmpty)
//    val result = if (matching.isEmpty) s"backlog is empty" :: Nil
//    else matching.reverseMap(i => i.render())
//    Out(result, None)
//  }

  private def onAddThing(args: List[String], currentModel: Model, refProvider: RefProvider) = {
    currentModel.createThing(args, None, None, refProvider) match {
      case Left(e) => Out(e, None)
      case Right(r) => Out(s"+ ${r.created.render()}" :: Nil, Some(r.updatedModel))
    }
  }

//  private def onAddAndBeginIssue(who: String, args: List[String], currentModel: Model, refProvider: RefProvider) = {
//    currentModel.createIssue(args, Some(currentModel.beginState), None, refProvider) match {
//      case Left(e) => Out(e, None)
//      case Right(r) => Out(Presentation.board(r.updatedModel), Some(r.updatedModel))
//    }
//  }
//
//  private def onAddAndForwardIssue(who: String, args: List[String], currentModel: Model, refProvider: RefProvider) = {
//    currentModel.createIssue(args, Some(currentModel.state(1)), Some(currentModel.aka(who)), refProvider) match {
//      case Left(e) => Out(e, None)
//      case Right(r) => Out(Presentation.board(r.updatedModel), Some(r.updatedModel))
//    }
//  }
//
//  private def onAddAndEndIssue(who: String, args: List[String], currentModel: Model, refProvider: RefProvider) = {
//    currentModel.createIssue(args, Some(currentModel.endState), Some(currentModel.aka(who)), refProvider) match {
//      case Left(e) => Out(e, None)
//      case Right(r) => Out(Presentation.board(r.updatedModel), Some(r.updatedModel))
//    }
//  }

  private def onAka(who: String, aka: String, currentModel: Model): Out = {
    if (aka.size > 3) return Out(Messages.problem("maximum 3 chars"), None)
    val updatedModel = currentModel.copy(userToAka = currentModel.userToAka.updated(who, aka.toUpperCase))
    Out(Messages.help(aka.toUpperCase), Some(updatedModel))
  }
}

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

object Controller {
  private var model = Persistence.load
  private val refProvider = RefProvider(if (model.things.isEmpty) 0 else model.things.map(_.ref.toLong).max)

  def process(who: String, req: Req): Box[LiftResponse] =
    JsonRequestHandler.handle(req)((json, req) ⇒ {
      synchronized {
        val value = CliRequestJson.deserialise(pretty(render(json))).value.toLowerCase.trim.replaceAll("\\|", "")
        Tracker("rem.tracking").track(who, value)
        val out = RemCommander.process(value, who, model, refProvider)
        out.updatedModel.foreach(m => {
          model = m
          Persistence.save(model)
        })
        t(out.messages)
      }

      //TODO:
      //show count of issues
      //show count of releases
    })
}

object Persistence {
  private val file = Paths.get("rem.json")
//  private val defaultStatuses = List("next", "doing", "done")

  def load: Model = {
    if (!file.toFile.exists()) save(Model(/*defaultStatuses,*/ immutable.Map[String, String](), List[Thing]()/*, List[Release]()*/))
    Json.deserialise(Filepath.load(file))
  }

  def save(state: Model) {
    Filepath.save(pretty(render(Json.serialise(state))), file)
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

///Rest

import net.liftweb.common.Full
import net.liftweb.http.rest.RestHelper
import net.liftweb.http._

object Rem extends RestHelper {
  import app.restlike.rem.Messages._
  import app.restlike.common.Responder._

  serve {
    case r@Req("rem" :: "install" :: Nil, _, GetRequest) ⇒ () ⇒ t(install, downcase = false)
    case r@Req("rem" :: "tracking" :: Nil, _, GetRequest) ⇒ () ⇒ t(Tracker("rem.tracking").view, downcase = false)
    case r@Req("rem" :: "state" :: Nil, _, GetRequest) ⇒ () ⇒ Full(JsonResponse(Json.serialise(Persistence.load)))
    case r@Req("rem" :: who :: Nil, _, PostRequest) ⇒ () ⇒ Controller.process(who, r)
  }
}
