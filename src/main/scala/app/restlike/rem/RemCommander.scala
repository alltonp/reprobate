package app.restlike.rem

import app.restlike.common.Colours._
import app.restlike.common._

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
