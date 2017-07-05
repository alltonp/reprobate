package app.restlike.rim

import server.ServiceFactory._
import app.restlike.common.Colours._
import app.restlike.common._

object Commander {
  def process(value: String, who: String, currentModel: Model, refProvider: RefProvider, token: String): Out = {
    val bits = value.split(" ").map(_.trim).filterNot(_.isEmpty)
    val cmd = In(bits.headOption, if (bits.isEmpty) Nil else bits.tail.toList)

    if (!cmd.head.getOrElse("").equals("aka") && !currentModel.knows_?(who)) return Out(Messages.notAuthorised(who), None, Nil)

    def aka = currentModel.aka(who)

    //TODO: be nice of the help could be driven off this ...
    cmd match {
      case In(None, Nil) => onShowBoard(currentModel, aka)
      case In(Some("aka"), List(myAka)) => onAka(who, myAka, currentModel)
      case In(Some("tags"), Nil) => onShowTagPriority(who, currentModel)
      case In(Some("tags"), args) if args.nonEmpty && args.head == "=" => onSetTagPriority(who, args.drop(1), currentModel)
      case In(Some("workflow"), args) if args.nonEmpty && args.head == "=" => onSetWorkflow(who, args.drop(1), currentModel)
      case In(Some("help"), Nil) => onHelp(currentModel, aka)
      case In(Some("+"), args) => onAddIssue(args, currentModel, refProvider)
      case In(Some("+/"), args) => onAddAndBeginIssue(args, currentModel, refProvider, aka)
      case In(Some("+//"), args) => onAddAndForwardIssue(args, currentModel, refProvider, aka)
      case In(Some("+!"), args) => onAddAndEndIssue(args, currentModel, refProvider, aka)
      case In(Some("?"), Nil) => onQueryIssues(currentModel, Nil, aka)
      case In(Some("?"), terms) => onQueryIssues(currentModel, terms, aka)
      case In(Some("."), Nil) => onShowPreWorkflowState(currentModel, aka)
      case In(Some("^"), providedTags) => onShowBoardManagementSummary(currentModel, providedTags, aka, sanitise = false)
      case In(Some("^_"), providedTags) => onShowBoardManagementSummary(currentModel, providedTags, aka, sanitise = true)
      case In(Some(release), args) if args.nonEmpty && args.head == "^" => onShowReleaseManagementSummary(release, currentModel, args.drop(1), aka, sanitise = false)
      case In(Some(release), args) if args.nonEmpty && args.head == "^_" => onShowReleaseManagementSummary(release, currentModel, args.drop(1), aka, sanitise = true)
      case In(Some(ref), List("-")) => onRemoveIssue(ref, currentModel)
      case In(Some(ref), args) if args.nonEmpty && args.head == "=" => onEditIssue(ref, args.drop(1), currentModel, aka)
      case In(Some(ref), args) if args.nonEmpty && args.forall(_.contains("=")) => onValueIssue(ref, args, currentModel, aka)
//      case In(Some(ref), args) if args.nonEmpty && args.forall(_.contains("+")) => onIncrementValue(ref, args, currentModel, aka)
      case In(Some(ref), List("/")) => onForwardIssue(ref, currentModel, aka)
      case In(Some(ref), List("/!")) => onFastForwardIssue(ref, currentModel, aka)
      case In(Some(ref), List(".")) => onBackwardIssue(ref, currentModel, aka)
      case In(Some(ref), List(".!")) => onFastBackwardIssue(ref, currentModel, aka)
      case In(Some(ref), List("%")) => onUnblockIssue(ref, currentModel, aka)
      case In(Some(ref), args) if args.size > 1 && args.head == "%" => onBlockIssue(ref, args.drop(1), currentModel, aka)
      case In(Some(ref), List("@")) => onOwnIssue(who, ref, currentModel, aka)
      case In(Some(ref), List("@-")) => onDisownIssue(who, ref, currentModel, aka)
      case In(Some(ref), args) if args.size == 2 && args.head == "@=" => onAssignIssue(args.drop(1).head.toUpperCase, ref, currentModel, aka)
      case In(Some(ref), args) if args.size == 2 && args.head == "_" => onMoveIssueUnder(Some(args.drop(1).head), ref, currentModel, aka)
      case In(Some(ref), args) if args.size == 1 && args.head == "_" => onMoveIssueUnder(None, ref, currentModel, aka)
      case In(Some(ref), args) if args.size > 1 && args.head == "," => onCommentOnIssue(args.drop(1), ref, currentModel, aka)
      case In(Some("@"), Nil) => onShowWhoIsDoingWhat(currentModel)
      case In(Some(ref), args) if args.nonEmpty && args.size > 1 && args.head == ":" => onTagIssue(ref, args.drop(1), currentModel, aka)
      case In(Some(ref), args) if args.nonEmpty && args.size > 1 && args.head == ":-" => onDetagIssue(ref, args.drop(1), currentModel, aka)
      case In(Some(":"), Nil) => onShowTags(currentModel)
      case In(Some(oldTag), args) if args.nonEmpty && args.size == 2 && args.head == ":=" => onMigrateTag(oldTag, args.drop(1).head, currentModel)
      case In(Some(tagToDelete), args) if args.nonEmpty && args.size == 1 && args.head == ":--" => onDeleteTagUsages(tagToDelete, currentModel)
      case In(Some(":"), args) if args.nonEmpty && args.size == 1 => onShowAllForTag(args.head, currentModel)
      case In(Some(":-"), Nil) => onShowUntagged(currentModel, aka)
      //or £
      case In(Some("±"), List(tag)) => onRelease(tag, currentModel, aka)
      case In(Some("±"), Nil) => onShowReleases(currentModel, aka)
//      case In(Some("note"), args) if args.nonEmpty && args.size == 1 => onShowReleaseNote(args.head, currentModel)
      case In(Some(ref), Nil) => onShowHistoryIssue(ref, currentModel, token)
      case In(head, tail) => onUnknownCommand(head, tail)
    }
  }

  private def onUnknownCommand(head: Option[String], tail: List[String]) =
    Out(customRed(Messages.eh) + " " + head.getOrElse("") + " " + tail.mkString(" ") :: Nil, None, Nil)

  private def onShowBoard(currentModel: Model, aka: String) = Out(Presentation.board(currentModel, Nil, aka), None, Nil)

  private def onHelp(currentModel: Model, aka: String) = Out(Messages.help(aka), None, Nil)

  private def onShowReleases(currentModel: Model, aka: String) = {
    val all = currentModel.released.reverse.flatMap(Presentation.release(currentModel, _, Some(aka)))
    val result = if (all.isEmpty) Messages.success(s"no releases found")
    else all
    Out(result, None, Nil)
  }

  private def onShowWhoIsDoingWhat(currentModel: Model) = {
    val akas = currentModel.akas
    val all = akas.map(aka => {
      val issues = currentModel.issues.filter(_.by == Some(aka))
      Presentation.issuesForUser(currentModel, aka, SortByStatus(issues, currentModel))
    })

    val result = if (all.isEmpty) Messages.success(s"nobody is doing anything")
    else all
    Out(result, None, Nil)
  }

  private def onShowHistoryIssue(ref: String, currentModel: Model, token: String) = {
    //TODO: history doesnt work on released .. is that oksy? msynbe it is?
    val all = Rim.history(token)
      .filter(_.refs.contains(ref))
      .filter(_.what.isDefined)
      .filter(_.who.isDefined)
      .filter(_.when.isDefined)

//    println(s"$ref:${all.size}")

//    val addable = List("+", "+/", "+//", "+!").map(Some(_))
//    val adds = Rim.history(token).filter(h => addable.contains(h.ref)).reverse

//    currentModel.allIssuesIncludingReleased.map(i => {
//      val r = adds.find(a => {
////        val d = a.action.fold("")(_.split(" ").init.mkString(" "))
//        val d = a.action.getOrElse("")
////        println(s"%${a.action} $d => ${i.description}")
//        d == i.name
//      })
////      println(s"$i => $r")
//      //TODO: at the end we should simulate an event ... using the added ...
//      //hmm .. not sure what top do in the case of +! etc
//      //maybe token should be at the start and support extra args for the ref on creation
//      r
//    })
    //(1) using just + +/ +// +!
    //for each model issue with created = None
    //find the first addable where description == issue.description
    //set created = Some(Created(addable.when, addable.who))

    //this bit is lossy ... if we went through backwards we could in theory migrate back
    //(2) repeat using =

    //save changes - will need to process issues and released seperately

    //TODO: report on issues that are created.isEmpty
    //TODO: run this is as part of rim {display} for a bit

//    println(adds.mkString("\n"))

    val issue = currentModel.allIssuesIncludingReleased.find(i => i.ref == ref)

    val result = if (issue.isEmpty) Messages.notFound(ref)
    else if (all.isEmpty) Messages.problem(s"no history for: $ref")
    else List(issue.get.render(currentModel)) ::: all.map(h => s"> ${dateFormats().fileDateTimeFormat.print(h.when.get)}: ${currentModel.aka(h.who.get)} ${h.what.get}").toList
    Out(result, None, Nil)
  }

  private def onShowTags(currentModel: Model) = {
    val all = currentModel.tags
    val result = if (all.isEmpty) Messages.success(s"no tags found")
    else Presentation.tags(all)
    Out(result, None, Nil)
  }

  private def onShowAllForTag(tag: String, currentModel: Model) = {
    val issuesWithTag = currentModel.allIssuesIncludingReleased.filter(_.tags.contains(tag))
    val result = if (issuesWithTag.isEmpty) Messages.success(s"no issues found for tag: $tag")
    else Presentation.tagDetail(tag, issuesWithTag, currentModel)
    Out(result, None, Nil)
  }

  private def onShowUntagged(currentModel: Model, aka: String) = {
    val untagged = currentModel.issues.filter(_.tags.isEmpty)
    val result = if (untagged.isEmpty) Messages.success(s"all issues have tags")
    else SortByStatus(untagged, currentModel).map(_.render(currentModel, highlightAka = Some(aka)))
    Out(result, None, Nil)
  }

//  private def onShowReleaseNote(release: String, currentModel: Model) = {
//    val maybeRelease = currentModel.released.find(_.tag == release)
//    val result = if (maybeRelease.isEmpty) Messages.problem(s"no release found for: $release")
//    else Presentation.releaseNotes(release, maybeRelease.get.issues, currentModel).toList
//    Out(result, None)
//  }

  private def onRelease(tag: String, currentModel: Model, aka: String): Out = {
    val releaseable = currentModel.releasableIssues
    val remainder = currentModel.issues diff releaseable

    if (currentModel.releaseTags.contains(tag)) return Out(Messages.problem(s"$tag has already been released"), None, Nil)
    if (releaseable.isEmpty) return Out(Messages.problem(s"nothing to release for $tag"), None, Nil)

    val release = Release(tag, releaseable.map(_.copy(status = None)), Some(systemClock().dateTime.getMillis))
    //TODO: this can die soon ...
//    val releasesToMigrate = currentModel.released.map(r => r.copy(issues = r.issues.map(i => i.copy(status = Some(currentModel.config.postWorkflowState)))))
    val updatedModel = currentModel.copy(issues = remainder, released = release :: currentModel.released/* :: releasesToMigrate */)

    Out(Presentation.release(currentModel, release, Some(aka)), Some(updatedModel), changed = releaseable.map(_.ref))
  }

  private def onMigrateTag(oldTag: String, newTag: String, currentModel: Model) = {
    def migrateTags(tags: Set[String]): Set[String] = tags - oldTag + newTag
    def migrateIssue(i: Issue): Issue = i.copy(tags = {
      val allTags = i.tags.getOrElse(Set.empty)
      val migratedTasgs = if (allTags.contains(oldTag)) migrateTags(allTags) else allTags
      if (migratedTasgs.isEmpty) None else Some(migratedTasgs)
    })

    if (oldTag.trim == newTag.trim) Out(Messages.problem(s"old tag and new tag must be different"), changed = Nil)
    else if (currentModel.tags.map(_.name).contains(oldTag)) {
      val updatedModel = currentModel.copy(
        issues = currentModel.issues.map(i => {
          migrateIssue(i)
        }),
        released = currentModel.released.map(r => {
          r.copy(issues = r.issues.map(i => migrateIssue(i)))
        })
      )
      //TODO: should show the issues that have changed as a result
      Out(Presentation.tags(updatedModel.tags), Some(updatedModel), changed = Nil)
    } else Out(Messages.problem(s"$oldTag does not exist"), changed = Nil)
  }

  private def onDeleteTagUsages(oldTag: String, currentModel: Model) = {
    def migrateTags(tags: Set[String]): Set[String] = tags - oldTag
    def migrateIssue(i: Issue): Issue = i.copy(tags = {
      val allTags = i.tags.getOrElse(Set.empty)
      val newTags = if (allTags.contains(oldTag)) migrateTags(allTags) else allTags
      if (newTags.isEmpty) None else Some(newTags)
    })

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
      Out(Presentation.tags(updatedModel.tags), Some(updatedModel), changed = Nil)
    } else Out(Messages.problem(s"$oldTag does not exist"), changed = Nil)
  }

  private def onDetagIssue(ref: String, args: List[String], currentModel: Model, aka: String) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None, Nil)){found =>
      val newTags = found.tags.getOrElse(Set.empty) -- args
      val updatedIssue = found.copy(tags = if (newTags.isEmpty) None else Some(newTags))
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.basedOnUpdateContext(updatedModel, updatedIssue, aka), Some(updatedModel), Seq(updatedIssue.ref))
    }
  }

  private def onTagIssue(ref: String, args: List[String], currentModel: Model, aka: String) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None, Nil)){found =>
      val newTags = found.tags.getOrElse(Set.empty) ++ args
      val updatedIssue = found.copy(tags = if (newTags.isEmpty) None else Some(newTags))
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.basedOnUpdateContext(updatedModel, updatedIssue, aka), Some(updatedModel), Seq(updatedIssue.ref))
    }
  }

  private def onOwnIssue(who: String, ref: String, currentModel: Model, aka: String) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None, Nil)){found =>
      val updatedIssue = found.copy(by = Some(currentModel.userToAka(who)))
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.basedOnUpdateContext(updatedModel, updatedIssue, aka), Some(updatedModel), Seq(updatedIssue.ref))
    }
  }

  private def onDisownIssue(who: String, ref: String, currentModel: Model, aka: String) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None, Nil)){found =>
      val updatedIssue = found.copy(by = None)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.basedOnUpdateContext(updatedModel, updatedIssue, aka), Some(updatedModel), Seq(updatedIssue.ref))
    }
  }

  private def onAssignIssue(assignee: String, ref: String, currentModel: Model, aka: String): Out = {
    if (!currentModel.userToAka.values.toSeq.contains(assignee)) return Out(Messages.problem(s"$assignee is not one of: ${currentModel.userToAka.values.mkString(", ")}"), changed = Nil)
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None, Nil)){found =>
      val updatedIssue = found.copy(by = Some(assignee))
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.basedOnUpdateContext(updatedModel, updatedIssue, aka), Some(updatedModel), Seq(updatedIssue.ref))
    }
  }

  private def onMoveIssueUnder(maybeUnderRef: Option[String], ref: String, currentModel: Model, aka: String): Out = {
    maybeUnderRef match {
      case Some(underRef) => {
        if (underRef == ref) return Out(Messages.problem(s"refs must different"), changed = Nil)

        if (currentModel.findIssue(underRef).isEmpty) return Out(Messages.notFound(underRef), changed = Nil)

        currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None, Nil)) { found =>
          val issuesWithRefRemoved = currentModel.issues.filterNot(_.ref == ref)
          val issueToBeUnder = issuesWithRefRemoved.find(_.ref == underRef).get
          val split: (List[Issue], List[Issue]) = issuesWithRefRemoved.splitAt(issuesWithRefRemoved.indexOf(issueToBeUnder) + 1)
          val newIssues = split._1 ++ List(found) ++ split._2
          val updatedModel = currentModel.copy(issues = newIssues)

          val presentation =
            if (found.status.getOrElse(-1) > 0) Presentation.board(updatedModel, Nil, aka)
            else Presentation.preWorkflowState(updatedModel, Some(aka))

          Out(presentation, Some(updatedModel), changed = Seq(ref, underRef))
        }

      }
      case None => {
        currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None, Nil)) { found =>
          val issuesWithRefRemoved = currentModel.issues.filterNot(_.ref == ref)
          val newIssues = List(found) ++ issuesWithRefRemoved
          val updatedModel = currentModel.copy(issues = newIssues)

          val presentation =
            if (found.status.getOrElse(-1) > 0) Presentation.board(updatedModel, Nil, aka)
            else Presentation.preWorkflowState(updatedModel, Some(aka))

          Out(presentation, Some(updatedModel), changed = Seq(ref))
        }

      }
    }
  }

  private def onCommentOnIssue(args: List[String], ref: String, currentModel: Model, aka: String): Out = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None, Nil)){found =>
      val comment = args.mkString(" ")
      val newComments = found.comments.getOrElse(Nil) ++ List(comment)
      val updatedIssue = found.copy(comments = Some(newComments))
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.basedOnUpdateContext(updatedModel, updatedIssue, aka), Some(updatedModel), Seq(updatedIssue.ref))
    }
  }

  //TODO: model.forwardAState
  //TODO: model.backwardAState
  private def onBackwardIssue(ref: String, currentModel: Model, aka: String) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None, Nil)){found =>
      val newStatus = if (found.status == Some(0)) Some(0)
      else {
//        val currentIndex = currentModel.config.workflowStates.indexOf(found.status.get)
//        if (currentIndex <= 0) None else Some(currentModel.config.workflowStates(currentIndex - 1))
        found.status.map(i => i -1)
      }
      val by = if (newStatus.getOrElse(999) < 2) None else Some(aka)
      val updatedIssue = found.copy(status = newStatus, by = by)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.board(updatedModel, Seq(ref), aka), Some(updatedModel), Seq(updatedIssue.ref))
    }
  }

  private def onBlockIssue(ref: String, args: List[String], currentModel: Model, aka: String) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None, Nil)){found =>
      val updatedIssue = found.copy(blocked = Some(args.mkString(" ")))
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.board(updatedModel, Seq(ref), aka), Some(updatedModel), Seq(updatedIssue.ref))
    }
  }

  private def onUnblockIssue(ref: String, currentModel: Model, aka: String) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None, Nil)){found =>
      val updatedIssue = found.copy(blocked = None)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.board(updatedModel, Seq(ref), aka), Some(updatedModel), Seq(updatedIssue.ref))
    }
  }

  private def onFastBackwardIssue(ref: String, currentModel: Model, aka: String) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None, Nil)){found =>
      val newStatus = Some(0)
      val updatedIssue = found.copy(status = newStatus, by = None)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.board(updatedModel, Seq(ref), aka), Some(updatedModel), Seq(updatedIssue.ref))
    }
  }

  private def onForwardIssue(ref: String, currentModel: Model, aka: String) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None, Nil)){found =>
      val newStatus = found.status.map(s => {
        if (s < currentModel.endStateIndex) s + 1 else s
      })
//      {
//        if (found.status.isEmpty) currentModel.beginState
//      else {
//        val currentIndex = found.status.get
//        val newIndex = if (currentIndex >= currentModel.config.workflowStates.size - 1) currentIndex else currentIndex + 1
        //        currentModel.config.workflowStates(newIndex)
//        newIndex
//      }
//      }
      val by = if (newStatus.getOrElse(999) < 2) None else Some(aka)
      val updatedIssue = found.copy(status = newStatus, by = by)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.board(updatedModel, Seq(ref), aka),
        Some(updatedModel), Seq(updatedIssue.ref))
    }
  }

  private def onFastForwardIssue(ref: String, currentModel: Model, aka: String) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None, Nil)){found =>
      val newStatus = currentModel.endStateIndex
      val updatedIssue = found.copy(status = Some(newStatus), by = Some(aka))
      val updatedModel = currentModel.updateIssue(updatedIssue)
      Out(Presentation.board(updatedModel, Seq(ref), aka), Some(updatedModel), Seq(updatedIssue.ref))
    }
  }

  private def onEditIssue(ref: String, args: List[String], currentModel: Model, aka: String) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None, Nil)){found =>
      val newDescription = args.mkString(" ")
      val updatedIssue = found.copy(name = newDescription)
      val updatedModel = currentModel.updateIssue(updatedIssue)
      //TODO: abstract this away somewhere
      //also, depended on context might want to show the preWorkflowState or releases
//      val presentation = if (updatedModel.onBoard_?(found)) Presentation.board(updatedModel, changed = Seq(found.ref), aka)
//                         else
//        Messages.successfulUpdate(s"${updatedIssue.render()}")
      Out(Presentation.basedOnUpdateContext(updatedModel, updatedIssue, aka), Some(updatedModel), Seq(updatedIssue.ref))
    }
  }

  private def onValueIssue(ref: String, args: List[String], currentModel: Model, aka: String) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None, Nil)){found =>
      val values = args.map(kv => {
        val b = kv.split("=")
        (b(0), b(1))
      }).toMap

      val existingValues = found.values.getOrElse(Map.empty)
      val mergedValues = (existingValues.keySet ++ values.keySet).map (i=> (
        i, if (values.contains(i)) values(i) else existingValues(i))
      ).toMap

      //TODO: when deleting, show a message if key does not exist ... key- or key=-
      //TODO: make it an Option[Map] .. for better json's when values are empty (check for and set to None)

      val toRemove = values.filter(_._2 == "-")
      val mergedAndDeleted = mergedValues.filterKeys(!toRemove.contains(_))
      val updatedIssue = found.copy(values = if (mergedAndDeleted.isEmpty) None else Some(mergedAndDeleted))
      val updatedModel = currentModel.updateIssue(updatedIssue)
      //TODO: abstract this away somewhere
      //also, depended on context might want to show the preWorkflowState or releases
//      val presentation = if (updatedModel.onBoard_?(found)) Presentation.board(updatedModel, changed = Seq(found.ref), aka)
//                         else
//        Messages.successfulUpdate(s"${updatedIssue.render()}")
      Out(Presentation.basedOnUpdateContext(updatedModel, updatedIssue, aka), Some(updatedModel), Seq(updatedIssue.ref))
    }
  }

//  private def onIncrementValue(ref: String, args: List[String], currentModel: Model, aka: String) = {
//    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None, Nil)){found =>
//      val existingValues = found.values.getOrElse(Map.empty)
//
//      //TODO: all sorts to worry about here ..
//      //- does key exist
//      //- is existing value a number
//      //- was passed in value a number
//      //-
//
//      val values = args.map(kv => {
//        val b = kv.split("+")
//        val value = if (b.size > 1) b(1) else "1"
//        (b(0), if (existingValues.contains(b(0))) b(1) else value)
//      }).toMap
//
//      val key = values.keys.head
//
//      if (!existingValues.contains(key)) {
//        Out(Messages.problem(s"property $key does not exist"), None, Nil)
//      } else if (values.values.head.toInt) {
//
//      }
//
//      val mergedValues = (existingValues.keySet ++ values.keySet).map (i=> (
//        i, if (values.contains(i)) values(i) else existingValues(i))
//      ).toMap
//
//      //TODO: when deleting, show a message if key does not exist ... key- or key=-
//      //TODO: make it an Option[Map] .. for better json's when values are empty (check for and set to None)
//
//      val toRemove = values.filter(_._2 == "-")
//      val mergedAndDeleted = mergedValues.filterKeys(!toRemove.contains(_))
//      val updatedIssue = found.copy(values = if (mergedAndDeleted.isEmpty) None else Some(mergedAndDeleted))
//      val updatedModel = currentModel.updateIssue(updatedIssue)
//      //TODO: abstract this away somewhere
//      //also, depended on context might want to show the preWorkflowState or releases
////      val presentation = if (updatedModel.onBoard_?(found)) Presentation.board(updatedModel, changed = Seq(found.ref), aka)
////                         else
////        Messages.successfulUpdate(s"${updatedIssue.render()}")
//      Out(Presentation.basedOnUpdateContext(updatedModel, updatedIssue, aka), Some(updatedModel), Seq(updatedIssue.ref))
//    }
//  }

  private def onRemoveIssue(ref: String, currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None, Nil)){found =>
      val updatedModel = currentModel.copy(issues = currentModel.issues.filterNot(i => i == found))
      Out(Messages.successfulUpdate(s"${found.render(currentModel)}"), Some(updatedModel), Nil)
    }
  }

  //TODO: add search to Model
  private def onQueryIssues(currentModel: Model, terms: List[String], aka: String) = {
    def query(issues: List[Issue], terms: List[String]): List[Issue] = {
      terms match {
        case Nil => issues
        case(ts) => query(issues.filter(i => i.search(ts.head, currentModel.config)), ts.tail)
      }
    }

    val matching = query(currentModel.allIssuesIncludingReleased, terms)
    val result = if (matching.isEmpty) (s"no issues found" + (if (terms.nonEmpty) s" for: ${terms.mkString(" ")}" else "")) :: Nil
    else SortByStatus(matching, currentModel).map(i => i.render(currentModel, highlightAka = Some(aka)))
    Out(result, None, Nil)
  }

  private def onShowPreWorkflowState(currentModel: Model, aka: String) = {
    Out(Presentation.preWorkflowState(currentModel, Some(aka)), None, Nil)
  }

  private def onShowBoardManagementSummary(currentModel: Model, providedTags: List[String], aka: String, sanitise: Boolean) = {
    val matching = currentModel.issues//.filterNot(i => i.status.isEmpty)
    onShowManagementSummary(matching, currentModel, providedTags, aka, sanitise)
  }

  private def onShowReleaseManagementSummary(release: String, currentModel: Model, providedTags: List[String], aka: String, sanitise: Boolean) = {
    val maybeRelease = currentModel.released.find(_.tag == release)
    maybeRelease match {
      case None => Out(Messages.problem(s"release $release does not exist"), None, Nil)
      case Some(r) => onShowManagementSummary(r.issues, currentModel, providedTags, aka, sanitise)
    }
  }

  private def onShowManagementSummary(matching: List[Issue], currentModel: Model, providedTags: List[String], aka: String, sanitise: Boolean) = {
    val blessedTags = if (providedTags.nonEmpty) providedTags else currentModel.config.priorityTags
    //TODO: this string will be wrong when we support releases - or maybe not
    //TODO: the empty check should be inside the Presentation
    val result = if (matching.isEmpty) s"board is empty" :: Nil
    //TODO: really really need display options
    else Presentation.pointyHairedManagerView(matching, blessedTags, currentModel, aka, sanitise, sanitise, sanitise, sanitise, sanitise).toList
    Out(result, None, Nil)
  }

  private def onAddIssue(args: List[String], currentModel: Model, refProvider: RefProvider) = {
    currentModel.createIssue(args, Some(0), None, refProvider) match {
      case Left(e) => Out(e, None, Nil)
      case Right(r) => Out(Messages.successfulUpdate(s"${r.created.render(currentModel)}"), Some(r.updatedModel), Seq(r.created.ref))
    }
  }

  private def onAddAndBeginIssue(args: List[String], currentModel: Model, refProvider: RefProvider, aka: String) = {
    currentModel.createIssue(args, Some(currentModel.beginState), None, refProvider) match {
      case Left(e) => Out(e, None, Nil)
      case Right(r) => Out(Presentation.board(r.updatedModel, Seq(r.created.ref), aka), Some(r.updatedModel), Seq(r.created.ref))
    }
  }

  private def onAddAndForwardIssue(args: List[String], currentModel: Model, refProvider: RefProvider, aka: String) = {
    //TODO: ultimately count the /
    currentModel.createIssue(args, Some(2), Some(aka), refProvider) match {
      case Left(e) => Out(e, None, Nil)
      case Right(r) => Out(Presentation.board(r.updatedModel, Seq(r.created.ref), aka), Some(r.updatedModel), Seq(r.created.ref))
    }
  }

  private def onAddAndEndIssue(args: List[String], currentModel: Model, refProvider: RefProvider, aka: String) = {
    currentModel.createIssue(args, Some(currentModel.endStateIndex), Some(aka), refProvider) match {
      case Left(e) => Out(e, None, Nil)
      case Right(r) => Out(Presentation.board(r.updatedModel, Seq(r.created.ref), aka), Some(r.updatedModel), Seq(r.created.ref))
    }
  }

  private def onAka(who: String, aka: String, currentModel: Model): Out = {
    if (aka.size > 3) return Out(Messages.problem("maximum 3 chars"), None, Nil)
    val updatedModel = currentModel.copy(userToAka = currentModel.userToAka.updated(who, aka.toUpperCase))
    Out(Messages.help(aka.toUpperCase), Some(updatedModel), Nil)
  }

  private def onSetTagPriority(who: String, tags: List[String], currentModel: Model): Out = {
    val updatedModel = currentModel.copy(config = currentModel.config.copy(priorityTags =  tags))
    //TODO: de-dupe message (using this version)
    Out(Messages.successfulUpdate(s"tag priority: ${if (updatedModel.config.priorityTags.isEmpty) "none" else updatedModel.config.priorityTags.mkString(" ")}"), Some(updatedModel), Nil)
  }

  private def onSetWorkflow(who: String, states: List[String], currentModel: Model): Out = {
    val updatedModel = currentModel.copy(config = currentModel.config.copy(workflowStates = states.map(State)))
    //TODO: de-dupe message (using this version)
    Out(Messages.successfulUpdate(s"workflow: ${if (updatedModel.config.workflowStates.isEmpty) "none" else updatedModel.config.workflowStates.map(_.name).mkString(" ")}"), Some(updatedModel), Nil)
  }

  private def onShowTagPriority(who: String, currentModel: Model): Out = {
    //TODO: de-dupe message (not this version)
    Out(Messages.success(s"tag priority: ${if (currentModel.config.priorityTags.isEmpty) "none" else currentModel.config.priorityTags.mkString(" ")}"), None, Nil)
  }
}

//TODO: move this out
object SortByStatus {
  def apply(issues: Seq[Issue], currentModel: Model) = {
//    val statusToIndex: Map[String, Int] = (currentModel.config.preWorkflowState :: currentModel.config.workflowStates ::: currentModel.config.postWorkflowState :: Nil).zipWithIndex.toMap
//    println(statusToIndex)
    issues.sortBy(i => i.status.getOrElse(999))
  }
}

