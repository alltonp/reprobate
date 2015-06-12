package app.restlike.rem

import app.restlike.common.Colours._
import app.restlike.common._

object Commander {
  def process(value: String, who: String, currentModel: Model, refProvider: RefProvider, user: String): Out = {
    val bits = value.split(" ").map(_.trim).filterNot(_.isEmpty)
    val cmd = In(bits.headOption, if (bits.isEmpty) Nil else bits.tail.toList)

//    if (!cmd.head.getOrElse("").equals("aka") && !currentModel.knows_?(who)) return Out(Messages.notAuthorised(who), None)

    //TODO: be nice of the help could be driven off this ...
    cmd match {
      //TODO: should propbably show somehting more useful, like most popular etc
//      case In(None, Nil) => onShowBoard(currentModel)
      case In(None, Nil) => onQueryThings(currentModel, Nil)
//      case In(Some("aka"), List(aka)) => onAka(who, aka, currentModel)
      case In(Some("help"), Nil) => onHelp(who, currentModel, user)
      case In(Some("+"), args) => onAddThing(args, currentModel, refProvider)
      case In(Some(ref), args) if args.nonEmpty && args.head == "_=" => onEditThingValue(ref, args.drop(1), currentModel)

      case In(Some("?"), Nil) => onQueryThings(currentModel, Nil)
      case In(Some("?"), terms) => onQueryThings(currentModel, terms)
      case In(Some(ref), List("-")) => onRemoveIssue(ref, currentModel)
//      case In(Some(ref), args) if args.nonEmpty && args.size > 1 && args.head == ":" => onTagIssue(ref, args.drop(1), currentModel)
//      case In(Some(ref), args) if args.nonEmpty && args.size > 1 && args.head == ":-" => onDetagIssue(ref, args.drop(1), currentModel)
//      case In(Some(oldTag), args) if args.nonEmpty && args.size == 2 && args.head == ":=" => onMigrateTag(oldTag, args.drop(1).head, currentModel)
//      case In(Some(":"), Nil) => onShowTags(currentModel)
      case In(head, tail) => onUnknownCommand(head, tail)
    }
  }

  private def onUnknownCommand(head: Option[String], tail: List[String]) =
    Out(red(Messages.eh) + " " + head.getOrElse("") + " " + tail.mkString(" ") :: Nil, None)

//  private def onShowBoard(currentModel: Model) = Out(Presentation.board(currentModel), None)

  private def onHelp(who: String, currentModel: Model, user: String) = Out(Messages.help(user), None)

  private def onShowTags(currentModel: Model) = {
    val all = currentModel.tags
    val result = if (all.isEmpty) s"no tags found" :: Nil
    else Presentation.tags(all)
    Out(result, None)
  }

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
    else Presentation.things(matching)
    Out(result, None)
  }

  private def onAddThing(args: List[String], currentModel: Model, refProvider: RefProvider) = {
    currentModel.createThing(args, None, None, refProvider) match {
      case Left(e) => Out(e, None)
      case Right(r) => Out(s"+ ${r.created.render()}" :: Nil, Some(r.updatedModel))
    }
  }

  private def onEditThingValue(ref: String, args: List[String], currentModel: Model) = {
    currentModel.findIssue(ref).fold(Out(Messages.notFound(ref), None)){found =>
      val newValue = args.mkString(" ")
      val updatedThing = found.copy(value = Some(newValue))
      val updatedModel = currentModel.updateIssue(updatedThing)
      Out(Presentation.things(updatedModel.things), Some(updatedModel))
    }
  }
}
