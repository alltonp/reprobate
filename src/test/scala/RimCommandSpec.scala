import app.restlike.rim._
import org.scalatest.{MustMatchers, WordSpec}

class RimCommandSpec extends WordSpec with MustMatchers {

//TODO: start with happy path
//  case In(Some(""), Nil) => onShowBoard(currentModel)
//  case In(Some("help"), Nil) => onHelp(who, currentModel)
//  case In(Some("?"), Nil) => onQueryIssues(currentModel, None)
//  case In(Some("?"), List(query)) => onQueryIssues(currentModel, Some(query))
//  case In(Some(ref), List("-")) => onRemoveIssue(ref, currentModel)
//  case In(Some(ref), args) if args.nonEmpty && args.head == "=" => onEditIssue(ref, args.drop(1), currentModel)
//  case In(Some(ref), List("/")) => onForwardIssue(who, ref, currentModel)
//  case In(Some(ref), List("/!")) => onFastForwardIssue(who, ref, currentModel)
//  case In(Some(ref), List(".")) => onBackwardIssue(who, ref, currentModel)
//  case In(Some(ref), List(".!")) => onFastBackwardIssue(who, ref, currentModel)
//  case In(Some(ref), List("@")) => onOwnIssue(who, ref, currentModel)
//  case In(Some(ref), args) if args.nonEmpty && args.size > 1 && args.head == ":" => onTagIssue(ref, args.drop(1), currentModel)
//  case In(Some(ref), args) if args.nonEmpty && args.size > 1 && args.head == ":-" => onDetagIssue(ref, args.drop(1), currentModel)
//  case In(Some("release"), List(tag)) => onRelease(tag, currentModel)
//  case In(Some("releases"), Nil) => onShowReleases(currentModel)
//  case In(head, tail) => onUnknownCommand(head, tail)

  "aka" in {
    val cmd = In(Some("aka"), List("a"))
    val current = Model(Nil, Map.empty, Nil, Nil)
    val expected = current.copy(userToAka = Map("anon" -> "A"))
    val out = Commander.process(cmd, "anon", current, RefProvider(0))
    out.updatedModel.mustEqual(Some(expected))
  }

  "+" in {
    val cmd = In(Some("+"), List("an", "item"))
    val current = Model(Nil, Map("anon" -> "A"), Nil, Nil)
    val expected = current.copy(issues = List(Issue("1", "an item", None, None)))
    val out = Commander.process(cmd, "anon", current, RefProvider(0))
    out.updatedModel.mustEqual(Some(expected))
  }

  "+/" in {
    val cmd = In(Some("+/"), List("an", "item"))
    val current = Model(List("next", "doing", "done"), Map("anon" -> "A"), Nil, Nil)
    val expected = current.copy(issues = List(Issue("1", "an item", Some("next"), Some("A"))))
    val out = Commander.process(cmd, "anon", current, RefProvider(0))
    out.updatedModel.mustEqual(Some(expected))
  }

  "+/!" in {
    val cmd = In(Some("+/!"), List("an", "item"))
    val current = Model(List("next", "doing", "done"), Map("anon" -> "A"), Nil, Nil)
    val expected = current.copy(issues = List(Issue("1", "an item", Some("done"), Some("A"))))
    val out = Commander.process(cmd, "anon", current, RefProvider(0))
    out.updatedModel.mustEqual(Some(expected))
  }

  "id /" in {
    val cmd = In(Some("1"), List("/"))
    val issue = Issue("1", "an item", None, None)
    val current = Model(List("next", "doing", "done"), Map("anon" -> "A"), List(issue), Nil)
    val expected = current.copy(issues = List(issue.copy(status = Some("next"), by = Some("A"))))
    val out = Commander.process(cmd, "anon", current, RefProvider(0))
    out.updatedModel.mustEqual(Some(expected))
  }

}
