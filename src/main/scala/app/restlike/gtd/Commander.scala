package app.restlike.gtd

import app.ServiceFactory.systemClock
import app.restlike.common.Colours._
import app.restlike.common._
import org.joda.time.LocalDate

object Commander {
  implicit def dateTimeOrdering: Ordering[LocalDate] = Ordering.fromLessThan(_ isBefore _)

  def process(value: String, who: String, currentModel: Model, refProvider: RefProvider): Out = {
    val bits = value.split(" ").map(_.trim).filterNot(_.isEmpty)
    val cmd = In(bits.headOption, if (bits.isEmpty) Nil else bits.tail.toList)

//    if (!cmd.head.getOrElse("").equals("aka") && !currentModel.knows_?(who)) return Out(Messages.notAuthorised(who), None)

//    def aka = currentModel.aka(who)

    //TODO: be nice of the help could be driven off this ...
    cmd match {
      case In(None, Nil) => onShowBoard(currentModel)
//      case In(Some("aka"), List(myAka)) => onAka(who, myAka, currentModel)
      case In(Some("tags"), Nil) => onShowTagPriority(who, currentModel)
      case In(Some("tags"), args) if args.nonEmpty && args.head == "=" => onSetTagPriority(who, args.drop(1), currentModel)
      case In(Some("help"), Nil) => onHelp(currentModel)
      case In(Some("+"), args) => onAddIssue(args, currentModel, refProvider)
//      case In(Some("+/"), args) => onAddAndBeginIssue(args, currentModel, refProvider, aka)
//      case In(Some("+//"), args) => onAddAndForwardIssue(args, currentModel, refProvider, aka)
//      case In(Some("+!"), args) => onAddAndEndIssue(args, currentModel, refProvider, aka)
      case In(Some("?"), Nil) => onQueryIssues(currentModel, Nil)
      case In(Some("?"), terms) => onQueryIssues(currentModel, terms)
//      case In(Some("."), Nil) => onShowBacklog(currentModel, aka)
//      case In(Some("^"), providedTags) => onShowBoardManagementSummary(currentModel, providedTags, aka, sanitise = false)
//      case In(Some("^_"), providedTags) => onShowBoardManagementSummary(currentModel, providedTags, aka, sanitise = true)
      case In(Some(ref), List("-")) => onRemoveIssue(ref, currentModel)
      case In(Some(ref), args) if args.nonEmpty && args.head == "=" => onEditIssue(ref, args.drop(1), currentModel)
      case In(Some(ref), List("!")) => onDoIssue(ref, currentModel)
      case In(Some(ref), List(".")) => onUndoIssue(ref, currentModel)
      case In(Some(ref), List("/")) => onNextIssue(ref, currentModel)
      case In(Some(ref), args) if args.nonEmpty && args.size > 1 && args.head == ":" => onTagIssue(ref, args.drop(1), currentModel)
//      case In(Some(ref), args) if args.nonEmpty && args.size > 1 && args.head == ":-" => onDetagIssue(ref, args.drop(1), currentModel, aka)
//      case In(Some(oldTag), args) if args.nonEmpty && args.size == 2 && args.head == ":=" => onMigrateTag(oldTag, args.drop(1).head, currentModel)
//      case In(Some(tagToDelete), args) if args.nonEmpty && args.size == 1 && args.head == ":--" => onDeleteTagUsages(tagToDelete, currentModel)
//      case In(Some(":"), Nil) => onShowTags(currentModel)
//      case In(Some(":"), args) if args.nonEmpty && args.size == 1 => onShowAllForTag(args.head, currentModel)
//      case In(Some(":-"), Nil) => onShowUntagged(currentModel, aka)
      case In(head, tail) => onUnknownCommand(head, tail)
    }
  }

  private def onUnknownCommand(head: Option[String], tail: List[String]) =
    Out(red(Messages.eh) + " " + head.getOrElse("") + " " + tail.mkString(" ") :: Nil, None)

  private def onShowBoard(currentModel: Model) = Out(Presentation.board(currentModel, Nil), None)

  private def onHelp(currentModel: Model) = Out(Messages.help("???"), None)

//  private def onShowTags(currentModel: Model) = {
//    val all = currentModel.tags
//    val result = if (all.isEmpty) Messages.success(s"no tags found")
//    else Presentation.tags(all)
//    Out(result, None)
//  }

//  private def onShowAllForTag(tag: String, currentModel: Model) = {
//    val issuesWithTag = currentModel.allIssuesIncludingReleased.filter(_.tags.contains(tag))
//    val result = if (issuesWithTag.isEmpty) Messages.success(s"no issues found for tag: $tag")
//    else Presentation.tagDetail(tag, issuesWithTag, currentModel)
//    Out(result, None)
//  }

//  private def onShowUntagged(currentModel: Model, aka: String) = {
//    val untagged = currentModel.things.filter(_.tags.isEmpty)
//    val result = if (untagged.isEmpty) Messages.success(s"all issues have tags")
////    else SortByStatus(untagged, currentModel).map(_.render(currentModel, highlightAka = Some(aka)))
//    Out(result, None)
//  }

//  private def onMigrateTag(oldTag: String, newTag: String, currentModel: Model) = {
//    def migrateTags(tags: Set[String]): Set[String] = tags - oldTag + newTag
//    def migrateIssue(i: Thing): Thing = i.copy(tags = if (i.tags.contains(oldTag)) migrateTags(i.tags) else i.tags)
//
//    if (oldTag.trim == newTag.trim) Out(Messages.problem(s"i would prefer it if the tags were different"))
//    else if (currentModel.tags.map(_.name).contains(oldTag)) {
//      val updatedModel = currentModel.copy(
//        things = currentModel.things.map(i => {
//          migrateIssue(i)
//        }),
//        done = currentModel.done.map(r => {
//          r.copy(issues = r.issues.map(i => migrateIssue(i)))
//        })
//      )
//      //TODO: should show the issues that have changed as a result
//      Out(Presentation.tags(updatedModel.tags), Some(updatedModel))
//    } else Out(Messages.problem(s"$oldTag does not exist"))
//  }

//  private def onDeleteTagUsages(oldTag: String, currentModel: Model) = {
//    def migrateTags(tags: Set[String]): Set[String] = tags - oldTag
//    def migrateIssue(i: Thing): Thing = i.copy(tags = if (i.tags.contains(oldTag)) migrateTags(i.tags) else i.tags)
//
//    if (currentModel.tags.map(_.name).contains(oldTag)) {
//      val updatedModel = currentModel.copy(
//        things = currentModel.things.map(i => {
//          migrateIssue(i)
//        }),
//        done = currentModel.done.map(r => {
//          r.copy(issues = r.issues.map(i => migrateIssue(i)))
//        })
//      )
//      //TODO: should show the issues that have changed as a result
//      Out(Presentation.tags(updatedModel.tags), Some(updatedModel))
//    } else Out(Messages.problem(s"$oldTag does not exist"))
//  }

//  private def onDetagIssue(ref: String, args: List[String], currentModel: Model, aka: String) = {
//    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
//      val newTags = found.tags -- args
//      val updatedIssue = found.copy(tags = newTags)
//      val updatedModel = currentModel.updateIssue(updatedIssue)
//      Out(Presentation.basedOnUpdateContext(updatedModel, updatedIssue, aka), Some(updatedModel))
//    }
//  }

  private def onTagIssue(ref: String, args: List[String], currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newTags = found.tags ++ args
      val updatedIssue = found.copy(tags = newTags)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.basedOnUpdateContext(updatedModel, updatedIssue), Some(updatedModel))
    }
  }

  private def onDoIssue(ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val updatedThings = currentModel.things.filterNot(_ == found)
      val updatedFound = found.copy(date = Some(systemClock().date))
      val updatedDone = updatedFound :: currentModel.done
      val updatedModel = currentModel.copy(things = updatedThings, done = updatedDone)
      Out(Presentation.board(updatedModel, Seq(ref)), Some(updatedModel))
    }
  }

  private def onUndoIssue(ref: String, currentModel: Model) = {
    currentModel.findDone(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val updatedThings = found :: currentModel.things
      val updatedDone = currentModel.done.filterNot(_ == found)
      val updatedModel = currentModel.copy(things = updatedThings, done = updatedDone)
      Out(Presentation.board(updatedModel, Seq(ref)), Some(updatedModel))
    }
  }

  private def onNextIssue(ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val updatedIssue = found.copy(date = Some(systemClock().date))
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.board(updatedModel, Seq(ref)), Some(updatedModel))
    }
  }

  private def onEditIssue(ref: String, args: List[String], currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newDescription = args.mkString(" ")
      val updatedIssue = found.copy(description = newDescription)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      //TODO: abstract this away somewhere
      //also, depended on context might want to show the backlog or releases
//      val presentation = if (updatedModel.onBoard_?(found)) Presentation.board(updatedModel, changed = Seq(found.ref), aka)
//                         else
//        Messages.successfulUpdate(s"${updatedIssue.render()}")
      Out(Presentation.basedOnUpdateContext(updatedModel, updatedIssue), Some(updatedModel))
    }
  }

  private def onRemoveIssue(ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val updatedModel = currentModel.copy(things = currentModel.things.filterNot(i => i == found))
      Out(Messages.successfulUpdate(s"${found.render(currentModel)}") ::: List("") ::: Presentation.board(updatedModel, Nil), Some(updatedModel))
    }
  }

//  //TODO: add search to Model
  private def onQueryIssues(currentModel: Model, terms: List[String]) = {
    def query(issues: List[Thing], terms: List[String]): List[Thing] = {
      terms match {
        case Nil => issues
        case(ts) => query(issues.filter(i => i.search(ts.head)), ts.tail)
      }
    }

    val matching = query(currentModel.allIssuesIncludingDone, terms)
    val result = if (matching.isEmpty) (s"no issues found" + (if (terms.nonEmpty) s" for: ${terms.mkString(" ")}" else "")) :: Nil
    else /*SortByStatus(matching, currentModel)*/matching.sortBy(_.date).map(i => i.render(currentModel))
    Out(result, None)
  }

  private def onShowBacklog(currentModel: Model, aka: String) = {
    val matching = currentModel.things.filter(i => i.date.isEmpty)
    val result = if (matching.isEmpty) s"backlog is empty" :: Nil
    else matching.map(i => i.render(currentModel, highlightAka = Some(aka)))
    Out(result, None)
  }

//  private def onShowBoardManagementSummary(currentModel: Model, providedTags: List[String], aka: String, sanitise: Boolean) = {
//    val matching = currentModel.things.filterNot(i => i.status.isEmpty)
//    onShowManagementSummary(matching, currentModel, providedTags, aka, sanitise)
//  }

//  private def onShowManagementSummary(matching: List[Thing], currentModel: Model, providedTags: List[String], aka: String, sanitise: Boolean) = {
//    val blessedTags = if (providedTags.nonEmpty) providedTags else currentModel.priorityTags
//    //TODO: this string will be wrong when we support releases - or maybe not
//    val result = if (matching.isEmpty) s"board is empty" :: Nil
//    else Presentation.pointyHairedManagerView("release", matching, blessedTags, currentModel, sanitise, aka).toList
//    Out(result, None)
//  }

  private def onAddIssue(args: List[String], currentModel: Model, refProvider: RefProvider) = {
    currentModel.createIssue(args, None, None, refProvider) match {
      case Left(e) => Out(e, None)
      case Right(r) => Out(Presentation.board(r.updatedModel, Seq(r.created.ref)), Some(r.updatedModel))
    }
  }

//  private def onAddAndBeginIssue(args: List[String], currentModel: Model, refProvider: RefProvider, aka: String) = {
//    currentModel.createIssue(args, Some(currentModel.beginState), None, refProvider) match {
//      case Left(e) => Out(e, None)
//      case Right(r) => Out(Presentation.board(r.updatedModel, Seq(r.created.ref), aka), Some(r.updatedModel))
//    }
//  }

//  private def onAddAndForwardIssue(args: List[String], currentModel: Model, refProvider: RefProvider, aka: String) = {
//    currentModel.createIssue(args, Some(currentModel.state(1)), Some(aka), refProvider) match {
//      case Left(e) => Out(e, None)
//      case Right(r) => Out(Presentation.board(r.updatedModel, Seq(r.created.ref), aka), Some(r.updatedModel))
//    }
//  }

//  private def onAddAndEndIssue(args: List[String], currentModel: Model, refProvider: RefProvider, aka: String) = {
//    currentModel.createIssue(args, Some(currentModel.endState), Some(aka), refProvider) match {
//      case Left(e) => Out(e, None)
//      case Right(r) => Out(Presentation.board(r.updatedModel, Seq(r.created.ref), aka), Some(r.updatedModel))
//    }
//  }

  private def onSetTagPriority(who: String, tags: List[String], currentModel: Model): Out = {
    val updatedModel = currentModel.copy(priorityTags = tags)
    //TODO: de-dupe message (using this version)
    Out(Messages.successfulUpdate(s"tag priority: ${if (updatedModel.priorityTags.isEmpty) "none" else updatedModel.priorityTags.mkString(" ")}"), Some(updatedModel))
  }

  private def onShowTagPriority(who: String, currentModel: Model): Out = {
    //TODO: de-dupe message (not this version)
    Out(Messages.success(s"tag priority: ${if (currentModel.priorityTags.isEmpty) "none" else currentModel.priorityTags.mkString(" ")}"), None)
  }
}

