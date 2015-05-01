package app.restlike.rim

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, StandardOpenOption}

import app.restlike.rim.Responder._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.json._
import org.joda.time.DateTime

import scala.collection.immutable
import scala.reflect.io.File

//NEXT:
//when doing +/ etc .. show the both the created ref and the new board
//show usage count next to tags (inc released)
//consolidate tags - e.g. bugs -> bug e.g. rim bugs := bug
//more expert mode options ...
//rim / - show begin
//rim // - show nth state
//rim ! - show end state
//Franck: tag many: `rim ref1 ref2 … refN : foo bar baz`
//properly support multiple / in /// and +///
//properly support multiple . in ...
//tags should be [a-z0-9\-]
//.sh needs a better message when not running
//store when released (eek, data change - so make it an option)
//show how long things have been in certain states
//show stats about akas ... entries, last used etc (top 5)
//show how long since aka X updated rim
//remove : from tags
//kill the :vg tag

//FUTURE:
//- private rims
//- grim
//- spartan bubble ui
//- hosted

//SOMEDAY/MAYBE:
//split and merge {}

object Messages {
  val eh = "- eh?"

  def notAuthorised(who: String) = List(s"- easy ${who}, please set your initials first ⇒ 'rim aka pa'")
  def notFound(ref: String) = problem(s"issue not found: $ref")
  def descriptionEmpty = problem(s"description is empty")
  def duplicateIssue(ref: String) = problem(s"issue already exists: $ref")
  def problem(message: String) = List(s"problem - $message")

  //TODO: how about advance and retreat instead of forward/back or push/pull or left/right
  def help(who: String) = List(
    s"hello ${who}, welcome to rim - rudimentary issue management © 2015 spabloshi ltd",
    "",
    "issues:",
    "  - create                        ⇒ 'rim + [the description] {: tag1 tag2 tagX}'",
    "  - update                        ⇒ 'rim [ref] ='",
    "  - delete                        ⇒ 'rim [ref] -'",
    "  - own                           ⇒ 'rim [ref] @'",
    "  - disown                        ⇒ 'rim [ref] @-'",
    "  - assign                        ⇒ 'rim [ref] @= [aka]'",
    "  - tag                           ⇒ 'rim [ref] : [tag1] {tag2} {tagX}'",
    "  - detag                         ⇒ 'rim [ref] :- [tag1] {tag2} {tagX}'",
    "  - move forward                  ⇒ 'rim [ref] /'",
//    "  - move forward many             ⇒ 'rim [ref] //'",
    "  - move to end                   ⇒ 'rim [ref] /!'",
    "  - move backward                 ⇒ 'rim [ref] .'",
    //    "  - move backward many      ⇒ 'rim [ref] ..'",
    "  - return to backlog             ⇒ 'rim [ref] .!'",
    "",
    "show:",
    "  - board                         ⇒ 'rim'",
    "  - backlog                       ⇒ 'rim .'",
    "  - all issues                    ⇒ 'rim ? {query}'",
    "  - releases                      ⇒ 'rim releases'",
    "  - tags                          ⇒ 'rim :'",
    "  - who is doing what             ⇒ 'rim @'",
    "  - help                          ⇒ 'rim help'",
    "",
    "other:",
    "  - set aka                       ⇒ 'rim aka [initials]'",
    "  - create release                ⇒ 'rim release [label]'",
    "",
    "expert mode:",
    "  - create, forward and tag       ⇒ 'rim +/ description {: tag1 tag2 tagX}'",
    "  - create, forward many and tag  ⇒ 'rim +// description {: tag1 tag2 tagX}'",
    "  - create, end and tag           ⇒ 'rim +/! description {: tag1 tag2 tagX}'",
    "",
    "where: [arg] = mandatory, {arg} = optional",
    ""
  )

  val install =
    (s"""#!/bin/bash
      |#INSTALLATION:
      |#- alias rim='{path to}/rim.sh'
      |#- that's it!
      |
      |RIM_HOST="http://${java.net.InetAddress.getLocalHost.getHostName}:8473"
      |""" + """OPTIONS="--timeout=15 --no-proxy -qO-"
      |WHO=`id -u -n`
      |BASE="rim/$WHO"
      |REQUEST="$OPTIONS $RIM_HOST/$BASE"
      |MESSAGE="${@:1}"
      |RESPONSE=`wget $REQUEST --post-data="{\"value\":\"${MESSAGE}\"}" --header=Content-Type:application/json`
      |printf "\n$RESPONSE\n\n"
      |
    """).stripMargin.split("\n").toList
}

case class RefProvider(initial: Long) {
  private var count = initial

  def next = synchronized {
    count += 1
    s"$count"
  }
}

case class Issue(ref: String, description: String, status: Option[String], by: Option[String], tags: Set[String] = Set.empty) {
  private val renderBy = by.fold("")(" @" + _)
  private val renderTags = tags.toList.sorted.map(t => s" :$t").mkString
  private val renderStatus = status.fold("")(" ^" + _)
  private val indexed = List(ref, description, renderStatus, renderBy.toLowerCase, renderTags).mkString(" ")

  def search(query: String) = indexed.contains(query)
  def render(hideStatus: Boolean = false, hideBy: Boolean = false) = s"$ref: $description${renderTags}${if (hideBy) "" else renderBy.toUpperCase}${if (hideStatus) "" else renderStatus}"
}

case class Release(tag: String, issues: List[Issue])

case class IssueCreation(created: Issue, updatedModel: Model)

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

  def tags = {
    val allIssues = released.map(_.issues).flatten ++ issues
    allIssues.map(_.tags).flatten.distinct.sorted
  }
}

case class In(head: Option[String], tail:List[String])
case class Out(messages: List[String] = Nil, updatedModel: Option[Model] = None)

object Commander {
  def process(value: String, who: String, currentModel: Model, refProvider: RefProvider): Out = {
    val bits = value.split(" ").map(_.trim).filterNot(_.isEmpty)
    val cmd = In(bits.headOption, if (bits.isEmpty) Nil else bits.tail.toList)

    if (!cmd.head.getOrElse("").equals("aka") && !currentModel.knows_?(who)) return Out(Messages.notAuthorised(who), None)

    //TODO: be nice of the help could be driven off this ...
    cmd match {
      case In(None, Nil) => onShowBoard(currentModel)
      case In(Some("aka"), List(aka)) => onAka(who, aka, currentModel)
      case In(Some("help"), Nil) => onHelp(who, currentModel)
      case In(Some("+"), args) => onAddIssue(args, currentModel, refProvider)
      case In(Some("+/"), args) => onAddAndBeginIssue(who, args, currentModel, refProvider)
      case In(Some("+//"), args) => onAddAndForwardIssue(who, args, currentModel, refProvider)
      case In(Some("+/!"), args) => onAddAndEndIssue(who, args, currentModel, refProvider)
      case In(Some("?"), Nil) => onQueryIssues(currentModel, None)
      case In(Some("?"), List(query)) => onQueryIssues(currentModel, Some(query))
      case In(Some("."), Nil) => onShowBacklog(currentModel)
      case In(Some(ref), List("-")) => onRemoveIssue(ref, currentModel)
      case In(Some(ref), args) if args.nonEmpty && args.head == "=" => onEditIssue(ref, args.drop(1), currentModel)
      case In(Some(ref), List("/")) => onForwardIssue(who, ref, currentModel)
      case In(Some(ref), List("/!")) => onFastForwardIssue(who, ref, currentModel)
      case In(Some(ref), List(".")) => onBackwardIssue(who, ref, currentModel)
      case In(Some(ref), List(".!")) => onFastBackwardIssue(who, ref, currentModel)
      case In(Some(ref), List("@")) => onOwnIssue(who, ref, currentModel)
      case In(Some(ref), List("@-")) => onDisownIssue(who, ref, currentModel)
      case In(Some(ref), args) if args.size == 2 && args.head == "@=" => onAssignIssue(args.drop(1).head.toUpperCase, ref, currentModel)
      case In(Some("@"), Nil) => onShowWhoIsDoingWhat(currentModel)
      case In(Some(ref), args) if args.nonEmpty && args.size > 1 && args.head == ":" => onTagIssue(ref, args.drop(1), currentModel)
      case In(Some(ref), args) if args.nonEmpty && args.size > 1 && args.head == ":-" => onDetagIssue(ref, args.drop(1), currentModel)
      case In(Some(":"), Nil) => onShowTags(currentModel)
      case In(Some("release"), List(tag)) => onRelease(tag, currentModel)
      case In(Some("releases"), Nil) => onShowReleases(currentModel)
      case In(head, tail) => onUnknownCommand(head, tail)
    }
  }

  private def onUnknownCommand(head: Option[String], tail: List[String]) =
    Out(Messages.eh + " " + head.getOrElse("") + " " + tail.mkString(" ") :: Nil, None)

  private def onShowBoard(currentModel: Model) = Out(Presentation.board(currentModel), None)

  private def onHelp(who: String, currentModel: Model) = Out(Messages.help(currentModel.aka(who)), None)

  private def onShowReleases(currentModel: Model) = {
    val all = currentModel.released.map(Presentation.release(_))
    val result = if (all.isEmpty) s"no releases found" :: Nil
    else all
    Out(result, None)
  }

  private def onShowWhoIsDoingWhat(currentModel: Model) = {
    val akas = currentModel.akas
    val all = akas.map(aka => {
      val issues = currentModel.issues.filter(_.by == Some(aka))
      Presentation.issuesForUser(aka, issues)
    })

    val result = if (all.isEmpty) s"nobody is doing anything" :: Nil
    else all
    Out(result, None)
  }

  private def onShowTags(currentModel: Model) = {
    val all = currentModel.tags
    val result = if (all.isEmpty) s"no tags found" :: Nil
    else ": " + all.mkString(", ") :: Nil
    Out(result, None)
  }

  private def onRelease(tag: String, currentModel: Model): Out = {
    val releaseable = currentModel.releasableIssues
    val remainder = currentModel.issues diff releaseable

    if (currentModel.releaseTags.contains(tag)) return Out(Messages.problem(s"$tag has already been released"), None)
    if (releaseable.isEmpty) return Out(Messages.problem(s"nothing to release for $tag"), None)

    val release = Release(tag, releaseable)
    val updatedModel = currentModel.copy(issues = remainder, released = release :: currentModel.released )

    Out(Presentation.release(release) :: Nil, Some(updatedModel))
  }

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

  private def onOwnIssue(who: String, ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val updatedIssue = found.copy(by = Some(currentModel.userToAka(who)))
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(s"@ ${updatedIssue.render()}" :: Nil, Some(updatedModel))
    }
  }

  private def onDisownIssue(who: String, ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val updatedIssue = found.copy(by = None)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(s"@ ${updatedIssue.render()}" :: Nil, Some(updatedModel))
    }
  }

  private def onAssignIssue(assignee: String, ref: String, currentModel: Model): Out = {
    if (!currentModel.userToAka.values.toSeq.contains(assignee)) return Out(Messages.problem(s"$assignee is not one of: ${currentModel.userToAka.values.mkString(", ")}"))
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val updatedIssue = found.copy(by = Some(assignee))
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(s"@ ${updatedIssue.render()}" :: Nil, Some(updatedModel))
    }
  }

  //TODO: model.forwardAState
  //TODO: model.backwardAState
  private def onBackwardIssue(who: String, ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newStatus = if (found.status.isEmpty) None
      else {
        val currentIndex = currentModel.workflowStates.indexOf(found.status.get)
        if (currentIndex <= 0) None else Some(currentModel.workflowStates(currentIndex - 1))
      }
      val by = if (newStatus.isEmpty || newStatus == Some(currentModel.beginState)) None else Some(currentModel.userToAka(who))
      val updatedIssue = found.copy(status = newStatus, by = by)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.board(updatedModel), Some(updatedModel))
    }
  }

  private def onFastBackwardIssue(who: String, ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newStatus = None
      val updatedIssue = found.copy(status = newStatus, by = None)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.board(updatedModel), Some(updatedModel))
    }
  }

  private def onForwardIssue(who: String, ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newStatus = if (found.status.isEmpty) currentModel.beginState
      else {
        val currentIndex = currentModel.workflowStates.indexOf(found.status.get)
        val newIndex = if (currentIndex >= currentModel.workflowStates.size - 1) currentIndex else currentIndex + 1
        currentModel.workflowStates(newIndex)
      }
      val by = if (newStatus == currentModel.beginState) None else Some(currentModel.userToAka(who))
      val updatedIssue = found.copy(status = Some(newStatus), by = by)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.board(updatedModel), Some(updatedModel))
    }
  }

  private def onFastForwardIssue(who: String, ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newStatus = currentModel.endState
      val updatedIssue = found.copy(status = Some(newStatus), by = Some(currentModel.userToAka(who)))
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.board(updatedModel), Some(updatedModel))
    }
  }

  private def onEditIssue(ref: String, args: List[String], currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newDescription = args.mkString(" ")
      val updatedIssue = found.copy(description = newDescription)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(s"= ${updatedIssue.render()}" :: Nil, Some(updatedModel))
    }
  }

  private def onRemoveIssue(ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val updatedModel = currentModel.copy(issues = currentModel.issues.filterNot(i => i == found))
      Out(s"- ${found.render()}" :: Nil, Some(updatedModel))
    }
  }

  //TODO: add search to Model
  private def onQueryIssues(currentModel: Model, query: Option[String]) = {
    val matching = query.fold(currentModel.issues)(q => currentModel.issues.filter(i => i.search(q)))
    val result = if (matching.isEmpty) (s"no issues found" + (if (query.isDefined) s" for: ${query.get}" else "")) :: Nil
    else matching.reverse.map(i => i.render())
    Out(result, None)
  }

  private def onShowBacklog(currentModel: Model) = {
    val matching = currentModel.issues.filter(i => i.status.isEmpty)
    val result = if (matching.isEmpty) s"backlog is empty" :: Nil
    else matching.reverse.map(i => i.render())
    Out(result, None)
  }

  private def onAddIssue(args: List[String], currentModel: Model, refProvider: RefProvider) = {
    currentModel.createIssue(args, None, None, refProvider) match {
      case Left(e) => Out(e, None)
      case Right(r) => Out(s"+ ${r.created.render()}" :: Nil, Some(r.updatedModel))
    }
  }

  private def onAddAndBeginIssue(who: String, args: List[String], currentModel: Model, refProvider: RefProvider) = {
    currentModel.createIssue(args, Some(currentModel.beginState), None, refProvider) match {
      case Left(e) => Out(e, None)
      case Right(r) => Out(Presentation.board(r.updatedModel), Some(r.updatedModel))
    }
  }

  private def onAddAndForwardIssue(who: String, args: List[String], currentModel: Model, refProvider: RefProvider) = {
    currentModel.createIssue(args, Some(currentModel.state(1)), None, refProvider) match {
      case Left(e) => Out(e, None)
      case Right(r) => Out(Presentation.board(r.updatedModel), Some(r.updatedModel))
    }
  }

  private def onAddAndEndIssue(who: String, args: List[String], currentModel: Model, refProvider: RefProvider) = {
    currentModel.createIssue(args, Some(currentModel.endState), Some(currentModel.aka(who)), refProvider) match {
      case Left(e) => Out(e, None)
      case Right(r) => Out(Presentation.board(r.updatedModel), Some(r.updatedModel))
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
  def board(model: Model) = {
    val stateToIssues = model.issues.groupBy(_.status)
    model.workflowStates.map(s => {
      val issuesForState = stateToIssues.getOrElse(Some(s), Nil)
      val issues = issuesForState.map(i => s"\n  ${i.render(hideStatus = true)}").mkString
      s"$s: (${issuesForState.size})" + issues + "\n"
    })
  }
  
  def release(release: Release) = {
    val r = release.issues.map(i => s"\n  ${i.render(hideStatus = true)}").mkString
    s"${release.tag}: (${release.issues.size})" + r + "\n"
  }

  def issuesForUser(aka: String, issues: List[Issue]) = {
    val r = issues.map(i => s"\n  ${i.render(hideBy = true)}").mkString
    s"${aka}: (${issues.size})" + r + "\n"
  }
}

object Controller {
  private var model = Persistence.load
  private val refProvider = RefProvider(if (model.issues.isEmpty) 0 else model.issues.map(_.ref.toLong).max)

  def process(who: String, req: Req): Box[LiftResponse] =
    JsonRequestHandler.handle(req)((json, req) ⇒ {
      synchronized {
        val value = RimRequestJson.deserialise(pretty(render(json))).value.toLowerCase.trim.replaceAll("\\|", "")
        Tracker.track(who, value)
        val out = Commander.process(value, who, model, refProvider)
        out.updatedModel.map(m => {
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

object Tracker {
  private val file = Paths.get("tracking.txt")

  def track(who: String, what: String) {
    val content = List(DateTime.now, who, what).mkString("|") + "\n"
    Filepath.append(content, file)
  }

  def view = Filepath.load(file).split("\n").reverse.toList
}

//TODO: use the one in little instead
object Filepath {
  def save(content: String, path: Path) = {
    Files.write(path, content.getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
  }

  def append(content: String, path: Path) = {
    Files.write(path, content.getBytes(StandardCharsets.UTF_8),
      StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)
  }

  def load(path: Path) = File(path.toFile).slurp()

  def create(path: Path) = Files.createFile(path)
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

